const MAX_DEVICES_PER_KEY = 3;

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

        if (kvData.status === "REVOKED" || kvData.status === "REFUNDED" || kvData.status === "EXPIRED") {
          return new Response(JSON.stringify({
            success: false,
            error: `License is ${kvData.status.toLowerCase()}`,
            status: kvData.status
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

        return new Response(JSON.stringify({
          success: true,
          tier: kvData.tier || "LIFETIME",
          deviceCount: devices.length,
          maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
          devices: devices
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
          return new Response(JSON.stringify({ success: false, error: "License key not found. Please ensure you purchased a valid license." }), {
            status: 404,
            headers: { "Content-Type": "application/json" }
          });
        }

        if (kvData.status === "REVOKED" || kvData.status === "REFUNDED" || kvData.status === "EXPIRED") {
          return new Response(JSON.stringify({
            success: false,
            error: `License is ${kvData.status.toLowerCase()}. Activation denied.`,
            status: kvData.status
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

          const devices = kvData.activatedDevices.map(d => ({
            deviceId: d.deviceId,
            deviceName: d.deviceName || "Android Device",
            activatedAt: d.activatedAt || 0,
            isCurrent: d.deviceId === deviceId
          }));

          return new Response(JSON.stringify({
            success: true,
            tier: kvData.tier,
            deviceCount: kvData.activatedDevices.length,
            maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
            devices: devices,
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

        const devices = kvData.activatedDevices.map(d => ({
          deviceId: d.deviceId,
          deviceName: d.deviceName || "Android Device",
          activatedAt: d.activatedAt || 0,
          isCurrent: d.deviceId === deviceId
        }));

        return new Response(JSON.stringify({
          success: true,
          tier: kvData.tier,
          deviceCount: kvData.activatedDevices.length,
          maxDevices: maxDevices,
          devices: devices,
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

        if (env.DODO_WEBHOOK_SECRET) {
          const isValid = await verifyStandardWebhookSignature(rawBody, request.headers, env.DODO_WEBHOOK_SECRET);
          if (!isValid) {
            return new Response(JSON.stringify({ success: false, error: "Invalid webhook signature" }), {
              status: 401,
              headers: { "Content-Type": "application/json" }
            });
          }
        }

        const eventData = JSON.parse(rawBody);
        const eventType = (eventData.type || eventData.event || "").toLowerCase();
        const payload = eventData.data || eventData.payload || eventData;

        const dodoKey = (
          payload.license_key ||
          (Array.isArray(payload.license_keys) ? payload.license_keys[0] : null) ||
          payload.license_key_instance?.key ||
          payload.licenses?.[0]?.key ||
          payload.licenseKey ||
          payload.key ||
          eventData.data?.license_key ||
          (Array.isArray(eventData.data?.license_keys) ? eventData.data.license_keys[0] : null) ||
          null
        );

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

        if (eventType === "refund.succeeded" || eventType === "dispute.opened" || eventType === "dispute.lost") {
          if (dodoKey) {
            const cleanKey = dodoKey.toString().trim().toUpperCase();
            const existing = await env.CIPHER_LICENSES.get(cleanKey, { type: "json" });
            if (existing) {
              existing.status = "REVOKED";
              existing.revokedReason = eventType;
              existing.revokedAt = Date.now();
              await env.CIPHER_LICENSES.put(cleanKey, JSON.stringify(existing));
            }
          }
          return new Response(JSON.stringify({ success: true, message: `License revoked due to ${eventType}` }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        if (eventType === "subscription.cancelled" || eventType === "subscription.expired") {
          if (dodoKey) {
            const cleanKey = dodoKey.toString().trim().toUpperCase();
            const existing = await env.CIPHER_LICENSES.get(cleanKey, { type: "json" });
            if (existing) {
              existing.status = "EXPIRED";
              existing.expiredReason = eventType;
              existing.expiredAt = Date.now();
              await env.CIPHER_LICENSES.put(cleanKey, JSON.stringify(existing));
            }
          }
          return new Response(JSON.stringify({ success: true, message: `License updated for ${eventType}` }), {
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
            }));
          }
          return new Response(JSON.stringify({
            success: false,
            error: "Webhook payload missing license_key. Flagged for manual review."
          }), {
            status: 200,
            headers: { "Content-Type": "application/json" }
          });
        }

        const licenseKey = dodoKey.toString().trim().toUpperCase();

        const existingRecord = await env.CIPHER_LICENSES.get(licenseKey, { type: "json" });
        if (existingRecord) {
          existingRecord.email = existingRecord.email || customerEmail || null;
          existingRecord.orderId = existingRecord.orderId || paymentId;
          existingRecord.tier = existingRecord.tier || mapped.tier;
          existingRecord.status = "ACTIVE";
          await env.CIPHER_LICENSES.put(licenseKey, JSON.stringify(existingRecord));
          await env.CIPHER_LICENSES.put(`ORDER:${paymentId}`, licenseKey);

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
          status: "ACTIVE",
          maxDevices: MAX_DEVICES_PER_KEY,
          activatedDevices: [],
          createdAt: Date.now()
        };

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
