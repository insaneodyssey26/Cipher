const MAX_DEVICES_PER_KEY = 3;
const EXPIRY_GRACE_MS = 3 * 24 * 60 * 60 * 1000;
const MISSING_KEY_TTL_SECONDS = 30 * 24 * 60 * 60;
const PENDING_SUBSCRIPTION_TTL_SECONDS = 7 * 24 * 60 * 60;
const MAX_FAILED_ACTIVATION_ATTEMPTS = 10;
const RATE_LIMIT_WINDOW_SECONDS = 60 * 60;
const DEFAULT_SIGNING_SECRET = "cipher-license-v1-k9f3x8b2m4q7w1z5p0";
const BLOCKED_STATUSES = ["REVOKED", "REFUNDED", "EXPIRED"];
const SUBSCRIPTION_RENEWAL_EVENTS = new Set([
  "subscription.active",
  "subscription.renewed",
  "subscription.updated",
  "subscription.plan_changed"
]);
const KEY_BEARING_EVENTS = new Set(["payment.succeeded", "license_key.created"]);

async function generateLicenseSignature(env, licenseKey, tier, deviceId, expiresAt) {
  const secret = (env && env.LICENSE_SIGNING_SECRET) || DEFAULT_SIGNING_SECRET;
  const canonical = `${(licenseKey || "").trim().toUpperCase()}:${(tier || "LIFETIME").trim().toUpperCase()}:${(deviceId || "").trim()}:${Number(expiresAt) || 0}`;
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const signature = await crypto.subtle.sign("HMAC", key, enc.encode(canonical));
  return Array.from(new Uint8Array(signature)).map(b => b.toString(16).padStart(2, "0")).join("");
}

async function checkFailedAttemptsRateLimit(env, clientIp) {
  if (!clientIp || clientIp === "unknown") return { blocked: false, count: 0 };
  const key = `RATELIMIT:FAIL:${clientIp}`;
  const record = await env.CIPHER_LICENSES.get(key, { type: "json" });
  if (record && typeof record.count === "number" && record.count >= MAX_FAILED_ACTIVATION_ATTEMPTS) {
    return { blocked: true, count: record.count };
  }
  return { blocked: false, count: record?.count || 0 };
}

async function recordFailedActivationAttempt(env, clientIp) {
  if (!clientIp || clientIp === "unknown") return;
  const key = `RATELIMIT:FAIL:${clientIp}`;
  const record = await env.CIPHER_LICENSES.get(key, { type: "json" });
  const count = (record?.count || 0) + 1;
  await env.CIPHER_LICENSES.put(key, JSON.stringify({ count: count, lastFailedAt: Date.now() }), {
    expirationTtl: RATE_LIMIT_WINDOW_SECONDS
  });
}

async function clearFailedActivationAttempts(env, clientIp) {
  if (!clientIp || clientIp === "unknown") return;
  const key = `RATELIMIT:FAIL:${clientIp}`;
  await env.CIPHER_LICENSES.delete(key);
}

function parseTimestampMs(value) {
  if (typeof value === "number") {
    return Number.isFinite(value) && value > 0 ? value : 0;
  }
  const parsed = Date.parse(value || "");
  return Number.isFinite(parsed) ? parsed : 0;
}

function blockedStatus(record, now = Date.now()) {
  if (BLOCKED_STATUSES.includes(record.status)) {
    return record.status;
  }
  if (record.expiresAt && now > record.expiresAt + EXPIRY_GRACE_MS) {
    return "EXPIRED";
  }
  return null;
}

function deriveSubscriptionUpdate(eventType, subscription, now = Date.now()) {
  const nextBilling = parseTimestampMs(subscription.next_billing_date);
  const cancelAtPeriodEnd = subscription.cancel_at_next_billing_date === true;

  if (eventType === "subscription.expired") {
    return { expire: true };
  }
  if (eventType === "subscription.cancelled") {
    if (nextBilling > now) {
      return { expire: false, expiresAt: nextBilling, cancelAtPeriodEnd: true };
    }
    return { expire: true };
  }
  if (SUBSCRIPTION_RENEWAL_EVENTS.has(eventType) && subscription.status === "active" && nextBilling > now) {
    return { expire: false, expiresAt: nextBilling, cancelAtPeriodEnd };
  }
  return null;
}

function applySubscriptionUpdate(record, update, eventType, now = Date.now()) {
  if (record.status !== "ACTIVE") {
    return false;
  }
  if (update.expire) {
    record.status = "EXPIRED";
    record.expiredReason = eventType;
    record.expiredAt = now;
    return true;
  }
  record.expiresAt = update.expiresAt;
  record.cancelAtPeriodEnd = update.cancelAtPeriodEnd;
  return true;
}

async function resolveLicenseKey(env, payload, directKey) {
  if (directKey) {
    return directKey;
  }
  const lookups = [];
  if (typeof payload.payment_id === "string" && payload.payment_id) {
    lookups.push(`ORDER:${payload.payment_id}`);
  }
  if (typeof payload.subscription_id === "string" && payload.subscription_id) {
    lookups.push(`SUB:${payload.subscription_id}`);
  }
  for (const lookup of lookups) {
    const indexed = await env.CIPHER_LICENSES.get(lookup);
    if (indexed) {
      return indexed;
    }
  }
  return null;
}

function base64ToUint8Array(base64) {
  const binaryString = atob(base64);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes;
}

function uint8ArrayToBase64(bytes) {
  let binary = "";
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

async function verifyStandardWebhookSignature(rawBody, headers, secret) {
  if (!secret) return false;

  const webhookId = headers.get("webhook-id");
  const webhookTimestamp = headers.get("webhook-timestamp");
  const webhookSignature = headers.get("webhook-signature") || headers.get("x-dodo-signature");

  if (!webhookId || !webhookTimestamp || !webhookSignature) {
    return false;
  }

  const timestampNum = parseInt(webhookTimestamp, 10);
  const now = Math.floor(Date.now() / 1000);
  if (isNaN(timestampNum) || Math.abs(now - timestampNum) > 300) {
    return false;
  }

  const signedContent = `${webhookId}.${webhookTimestamp}.${rawBody}`;
  const encoder = new TextEncoder();
  const signedContentBytes = encoder.encode(signedContent);

  let secretKeyBytes;
  if (secret.startsWith("whsec_")) {
    secretKeyBytes = base64ToUint8Array(secret.slice(6));
  } else {
    try {
      secretKeyBytes = base64ToUint8Array(secret);
    } catch {
      secretKeyBytes = encoder.encode(secret);
    }
  }

  const cryptoKey = await crypto.subtle.importKey(
    "raw",
    secretKeyBytes,
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signatureBuffer = await crypto.subtle.sign("HMAC", cryptoKey, signedContentBytes);
  const expectedSignatureBase64 = uint8ArrayToBase64(new Uint8Array(signatureBuffer));

  const signatures = webhookSignature.split(" ");
  for (const sigItem of signatures) {
    const parts = sigItem.split(",");
    const sig = parts.length === 2 ? parts[1] : parts[0];
    if (sig === expectedSignatureBase64) {
      return true;
    }
  }

  return false;
}

const PRODUCT_TIER_MAP = {
  "pdt_0nnvfqxm1f1vltvqvaiyw": { tier: "MONTHLY", prefix: "CIPHER-MONTHLY" },
  "pdt_0nnvgnp6fu53zp3izwst8": { tier: "HALF_YEARLY", prefix: "CIPHER-6MONTH" },
  "pdt_0nnvgcewj9jy1oyrs2kwf": { tier: "ANNUAL", prefix: "CIPHER-ANNUAL" },
  "pdt_0nnvgqmxn76k14c90kljx": { tier: "LIFETIME", prefix: "CIPHER-LIFETIME" }
};

function toLicenseKeyString(candidate) {
  if (typeof candidate === "string") {
    return candidate.trim();
  }
  if (candidate && typeof candidate === "object") {
    return toLicenseKeyString(candidate.key || candidate.license_key || candidate.value);
  }
  return "";
}

function extractLicenseKey(candidates) {
  for (const candidate of candidates) {
    const key = toLicenseKeyString(candidate);
    if (key) {
      return key.toUpperCase();
    }
  }
  return null;
}

function resolveProductTier(productId, productName) {
  const cleanId = (productId || "").trim().toLowerCase();
  if (PRODUCT_TIER_MAP[cleanId]) {
    return PRODUCT_TIER_MAP[cleanId];
  }

  const cleanName = (productName || "").trim().toUpperCase();
  if (cleanName.includes("MONTHLY") || cleanId.includes("monthly")) {
    return { tier: "MONTHLY", prefix: "CIPHER-MONTHLY" };
  }
  if (cleanName.includes("6-MONTH") || cleanName.includes("6MONTH") || cleanName.includes("HALF") || cleanId.includes("6month") || cleanId.includes("half")) {
    return { tier: "HALF_YEARLY", prefix: "CIPHER-6MONTH" };
  }
  if (cleanName.includes("ANNUAL") || cleanName.includes("YEAR") || cleanId.includes("annual") || cleanId.includes("year")) {
    return { tier: "ANNUAL", prefix: "CIPHER-ANNUAL" };
  }
  return { tier: "LIFETIME", prefix: "CIPHER-LIFETIME" };
}

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const clientIp = request.headers.get("cf-connecting-ip") || "unknown";

    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, Authorization, webhook-id, webhook-timestamp, webhook-signature, x-dodo-signature"
        }
      });
    }

    if (url.pathname === "/health") {
      return new Response(JSON.stringify({ status: "ok", service: "Cipher License Engine" }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    if (url.pathname === "/api/devices" && request.method === "GET") {
      try {
        const licenseKey = (url.searchParams.get("licenseKey") || "").trim().toUpperCase();
        const currentDeviceId = (url.searchParams.get("deviceId") || "").trim();

        if (!licenseKey) {
          return new Response(JSON.stringify({ success: false, error: "License key is required" }), {
            status: 400,
            headers: { "Content-Type": "application/json" }
          });
        }

        const kvData = await env.CIPHER_LICENSES.get(licenseKey, { type: "json" });
        if (!kvData) {
          return new Response(JSON.stringify({ success: false, error: "License not found" }), {
            status: 404,
            headers: { "Content-Type": "application/json" }
          });
        }

        const blocked = blockedStatus(kvData);
        if (blocked) {
          return new Response(JSON.stringify({
            success: false,
            error: `License is ${blocked.toLowerCase()}`,
            status: blocked
          }), {
            status: 403,
            headers: { "Content-Type": "application/json" }
          });
        }

        const devices = (kvData.activatedDevices || []).map(d => ({
          deviceId: d.deviceId,
          deviceName: d.deviceName || "Android Device",
          activatedAt: d.activatedAt || 0,
          isCurrent: currentDeviceId ? d.deviceId === currentDeviceId : false
        }));

        const signature = await generateLicenseSignature(
          env,
          licenseKey,
          kvData.tier || "LIFETIME",
          currentDeviceId,
          kvData.expiresAt || 0
        );

        return new Response(JSON.stringify({
          success: true,
          tier: kvData.tier || "LIFETIME",
          expiresAt: kvData.expiresAt || 0,
          deviceCount: devices.length,
          maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
          devices: devices,
          signature: signature
        }), {
          headers: { "Content-Type": "application/json" }
        });
      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    if (url.pathname === "/api/activate" && request.method === "POST") {
      try {
        const rateLimit = await checkFailedAttemptsRateLimit(env, clientIp);
        if (rateLimit.blocked) {
          return new Response(JSON.stringify({
            success: false,
            error: "Too many failed activation attempts. Please try again later."
          }), {
            status: 429,
            headers: {
              "Content-Type": "application/json",
              "Retry-After": "3600"
            }
          });
        }

        const body = await request.json();
        const licenseKey = (body.licenseKey || "").trim().toUpperCase();
        const email = (body.email || "").trim().toLowerCase();
        const deviceId = (body.deviceId || "").trim();
        const deviceName = (body.deviceName || "Android Device").trim();

        if (!licenseKey || !deviceId) {
          return new Response(JSON.stringify({ success: false, error: "License key and device ID are required" }), {
            status: 400,
            headers: { "Content-Type": "application/json" }
          });
        }

        const kvData = await env.CIPHER_LICENSES.get(licenseKey, { type: "json" });
        if (!kvData) {
          await recordFailedActivationAttempt(env, clientIp);
          return new Response(JSON.stringify({ success: false, error: "License key not found. Please ensure you purchased a valid license." }), {
            status: 404,
            headers: { "Content-Type": "application/json" }
          });
        }

        const blocked = blockedStatus(kvData);
        if (blocked) {
          await recordFailedActivationAttempt(env, clientIp);
          return new Response(JSON.stringify({
            success: false,
            error: `License is ${blocked.toLowerCase()}. Activation denied.`,
            status: blocked
          }), {
            status: 403,
            headers: { "Content-Type": "application/json" }
          });
        }

        if (!Array.isArray(kvData.activatedDevices)) {
          kvData.activatedDevices = [];
        }

        const existingDeviceIndex = kvData.activatedDevices.findIndex(d => d.deviceId === deviceId);

        if (existingDeviceIndex >= 0) {
          kvData.activatedDevices[existingDeviceIndex].lastSeenAt = Date.now();
          if (deviceName && deviceName !== "Android Device") {
            kvData.activatedDevices[existingDeviceIndex].deviceName = deviceName;
          }
          await env.CIPHER_LICENSES.put(licenseKey, JSON.stringify(kvData));
          await clearFailedActivationAttempts(env, clientIp);

          const devices = kvData.activatedDevices.map(d => ({
            deviceId: d.deviceId,
            deviceName: d.deviceName || "Android Device",
            activatedAt: d.activatedAt || 0,
            isCurrent: d.deviceId === deviceId
          }));

          const signature = await generateLicenseSignature(
            env,
            licenseKey,
            kvData.tier,
            deviceId,
            kvData.expiresAt || 0
          );

          return new Response(JSON.stringify({
            success: true,
            tier: kvData.tier,
            expiresAt: kvData.expiresAt || 0,
            deviceCount: kvData.activatedDevices.length,
            maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
            devices: devices,
            signature: signature,
            message: "Cipher Pro restored on this device."
          }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        const maxDevices = kvData.maxDevices || MAX_DEVICES_PER_KEY;
        if (kvData.activatedDevices.length >= maxDevices) {
          return new Response(JSON.stringify({
            success: false,
            error: `Device limit reached (${kvData.activatedDevices.length}/${maxDevices} devices active). Please revoke an old device.`
          }), {
            status: 403,
            headers: { "Content-Type": "application/json" }
          });
        }

        kvData.activatedDevices.push({
          deviceId: deviceId,
          deviceName: deviceName,
          activatedAt: Date.now(),
          lastSeenAt: Date.now()
        });

        if (email && !kvData.email) {
          kvData.email = email;
        }

        await env.CIPHER_LICENSES.put(licenseKey, JSON.stringify(kvData));
        await clearFailedActivationAttempts(env, clientIp);

        const devices = kvData.activatedDevices.map(d => ({
          deviceId: d.deviceId,
          deviceName: d.deviceName || "Android Device",
          activatedAt: d.activatedAt || 0,
          isCurrent: d.deviceId === deviceId
        }));

        const signature = await generateLicenseSignature(
          env,
          licenseKey,
          kvData.tier,
          deviceId,
          kvData.expiresAt || 0
        );

        return new Response(JSON.stringify({
          success: true,
          tier: kvData.tier,
          expiresAt: kvData.expiresAt || 0,
          deviceCount: kvData.activatedDevices.length,
          maxDevices: maxDevices,
          devices: devices,
          signature: signature,
          message: "Cipher Pro successfully activated!"
        }), {
          headers: { "Content-Type": "application/json" }
        });

      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    if ((url.pathname === "/api/revoke" || url.pathname === "/api/deactivate") && request.method === "POST") {
      try {
        const body = await request.json();
        const licenseKey = (body.licenseKey || "").trim().toUpperCase();
        const deviceId = (body.deviceId || "").trim();

        if (!licenseKey || !deviceId) {
          return new Response(JSON.stringify({ success: false, error: "License key and device ID are required" }), {
            status: 400,
            headers: { "Content-Type": "application/json" }
          });
        }

        const kvData = await env.CIPHER_LICENSES.get(licenseKey, { type: "json" });
        if (!kvData) {
          return new Response(JSON.stringify({ success: false, error: "License not found" }), {
            status: 404,
            headers: { "Content-Type": "application/json" }
          });
        }

        if (Array.isArray(kvData.activatedDevices)) {
          kvData.activatedDevices = kvData.activatedDevices.filter(d => d.deviceId !== deviceId);
          await env.CIPHER_LICENSES.put(licenseKey, JSON.stringify(kvData));
        }

        const devices = (kvData.activatedDevices || []).map(d => ({
          deviceId: d.deviceId,
          deviceName: d.deviceName || "Android Device",
          activatedAt: d.activatedAt || 0,
          isCurrent: false
        }));

        return new Response(JSON.stringify({
          success: true,
          deviceCount: devices.length,
          maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
          devices: devices,
          message: "Device successfully revoked"
        }), {
          headers: { "Content-Type": "application/json" }
        });
      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    const isWebhookPath = (
      url.pathname === "/api/webhook/dodo" ||
      url.pathname === "/api/webhook" ||
      url.pathname === "/webhook" ||
      url.pathname === "/dodo" ||
      url.pathname === "/api/dodo" ||
      url.pathname === "/webhook/dodo"
    );

    if (isWebhookPath && request.method === "GET") {
      return new Response(JSON.stringify({ status: "ok", service: "Dodo Webhook Receiver" }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    if (isWebhookPath && request.method === "POST") {
      try {
        const rawBody = await request.text();

        if (!env.DODO_WEBHOOK_SECRET) {
          console.error("[DODO WEBHOOK] DODO_WEBHOOK_SECRET is not configured; refusing to process webhooks");
          return new Response(JSON.stringify({ success: false, error: "Webhook receiver is not configured" }), {
            status: 503,
            headers: { "Content-Type": "application/json" }
          });
        }

        const isValid = await verifyStandardWebhookSignature(rawBody, request.headers, env.DODO_WEBHOOK_SECRET);
        if (!isValid) {
          return new Response(JSON.stringify({ success: false, error: "Invalid webhook signature" }), {
            status: 401,
            headers: { "Content-Type": "application/json" }
          });
        }

        const eventData = JSON.parse(rawBody);
        const eventType = (eventData.type || eventData.event || "").toLowerCase();
        const payload = eventData.data || eventData.payload || eventData;

        const dodoKey = extractLicenseKey([
          payload.license_key,
          Array.isArray(payload.license_keys) ? payload.license_keys[0] : null,
          payload.license_key_instance?.key,
          payload.licenses?.[0]?.key,
          payload.licenseKey,
          payload.key,
          eventData.data?.license_key,
          Array.isArray(eventData.data?.license_keys) ? eventData.data.license_keys[0] : null
        ]);

        const paymentId = payload.payment_id || payload.id || `TXN_${Date.now()}`;
        const customerEmail = (
          payload.customer?.email ||
          payload.billing?.email ||
          payload.email ||
          payload.customer_email ||
          eventData.customer?.email ||
          ""
        ).trim().toLowerCase();

        const productId = (payload.product_id || payload.items?.[0]?.product_id || "").toLowerCase();
        const productName = (payload.product_name || payload.items?.[0]?.product_name || "").toUpperCase();
        const mapped = resolveProductTier(productId, productName);

        const subscriptionId = typeof payload.subscription_id === "string" ? payload.subscription_id : "";

        if (eventType === "refund.succeeded" || eventType === "dispute.opened" || eventType === "dispute.lost") {
          const targetKey = await resolveLicenseKey(env, payload, dodoKey);
          if (targetKey) {
            const existing = await env.CIPHER_LICENSES.get(targetKey, { type: "json" });
            if (existing) {
              existing.status = "REVOKED";
              existing.revokedReason = eventType;
              existing.revokedAt = Date.now();
              await env.CIPHER_LICENSES.put(targetKey, JSON.stringify(existing));
            }
          }
          return new Response(JSON.stringify({ success: true, message: `License revoked due to ${eventType}` }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        if (eventType.startsWith("subscription.")) {
          const update = deriveSubscriptionUpdate(eventType, payload);
          if (update) {
            const targetKey = await resolveLicenseKey(env, payload, dodoKey);
            const record = targetKey ? await env.CIPHER_LICENSES.get(targetKey, { type: "json" }) : null;
            if (record) {
              if (applySubscriptionUpdate(record, update, eventType)) {
                await env.CIPHER_LICENSES.put(targetKey, JSON.stringify(record));
              }
            } else if (!update.expire && subscriptionId) {
              await env.CIPHER_LICENSES.put(`SUBSCRIPTION_PENDING:${subscriptionId}`, JSON.stringify(update), { expirationTtl: PENDING_SUBSCRIPTION_TTL_SECONDS });
            }
          }
          return new Response(JSON.stringify({ success: true, message: `Subscription event ${eventType} processed` }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        if (!dodoKey && !KEY_BEARING_EVENTS.has(eventType)) {
          return new Response(JSON.stringify({ success: true, ignored: true }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        if (!dodoKey) {
          console.error(`[DODO WEBHOOK ALERT] Payment received without license_key. OrderId: ${paymentId}, Customer: ${customerEmail}, Product: ${productId}`);
          if (env.CIPHER_LICENSES && paymentId) {
            await env.CIPHER_LICENSES.put(`MISSING_KEY:${paymentId}`, JSON.stringify({
              orderId: paymentId,
              email: customerEmail,
              productId: productId,
              productName: productName,
              receivedAt: Date.now(),
              error: "Dodo license key feature was not triggered on this product"
            }), { expirationTtl: MISSING_KEY_TTL_SECONDS });
          }
          return new Response(JSON.stringify({
            success: false,
            error: "Webhook payload missing license_key. Flagged for manual review."
          }), {
            status: 200,
            headers: { "Content-Type": "application/json" }
          });
        }

        const licenseKey = dodoKey;

        await env.CIPHER_LICENSES.delete(`MISSING_KEY:${paymentId}`);

        const existingRecord = await env.CIPHER_LICENSES.get(licenseKey, { type: "json" });
        if (existingRecord) {
          existingRecord.email = existingRecord.email || customerEmail || null;
          existingRecord.orderId = existingRecord.orderId || paymentId;
          existingRecord.tier = existingRecord.tier || mapped.tier;
          existingRecord.status = existingRecord.status || "ACTIVE";
          existingRecord.subscriptionId = existingRecord.subscriptionId || subscriptionId || null;
          await env.CIPHER_LICENSES.put(licenseKey, JSON.stringify(existingRecord));
          await env.CIPHER_LICENSES.put(`ORDER:${paymentId}`, licenseKey);
          if (subscriptionId) {
            await env.CIPHER_LICENSES.put(`SUB:${subscriptionId}`, licenseKey);
          }

          return new Response(JSON.stringify({
            success: true,
            licenseKey: licenseKey,
            tier: existingRecord.tier,
            idempotent: true
          }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        const licenseRecord = {
          key: licenseKey,
          tier: mapped.tier,
          email: customerEmail || null,
          orderId: paymentId,
          productId: productId,
          subscriptionId: subscriptionId || null,
          status: "ACTIVE",
          maxDevices: MAX_DEVICES_PER_KEY,
          activatedDevices: [],
          createdAt: Date.now()
        };

        if (subscriptionId) {
          const pending = await env.CIPHER_LICENSES.get(`SUBSCRIPTION_PENDING:${subscriptionId}`, { type: "json" });
          if (pending) {
            applySubscriptionUpdate(licenseRecord, pending, "subscription.pending");
            await env.CIPHER_LICENSES.delete(`SUBSCRIPTION_PENDING:${subscriptionId}`);
          }
          await env.CIPHER_LICENSES.put(`SUB:${subscriptionId}`, licenseKey);
        }

        await env.CIPHER_LICENSES.put(licenseKey, JSON.stringify(licenseRecord));
        await env.CIPHER_LICENSES.put(`ORDER:${paymentId}`, licenseKey);
        if (customerEmail) {
          await env.CIPHER_LICENSES.put(`EMAIL:${customerEmail}`, licenseKey);
        }

        return new Response(JSON.stringify({
          success: true,
          licenseKey: licenseKey,
          tier: mapped.tier,
          deepLink: `cipher://activate?key=${licenseKey}`
        }), {
          headers: { "Content-Type": "application/json" }
        });

      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    return new Response(JSON.stringify({ error: "Endpoint not found" }), {
      status: 404,
      headers: { "Content-Type": "application/json" }
    });
  }
};
