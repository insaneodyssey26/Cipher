import { describe, it, expect, beforeEach } from "vitest";
import worker from "./cipher_license_worker.js";

class MockKV {
  constructor() {
    this.store = new Map();
  }
  async get(key, options) {
    const val = this.store.get(key);
    if (!val) return null;
    if (options && options.type === "json") {
      try {
        return JSON.parse(val);
      } catch {
        return val;
      }
    }
    return val;
  }
  async put(key, val, options) {
    this.store.set(key, typeof val === "string" ? val : JSON.stringify(val));
    this.options = this.options || new Map();
    this.options.set(key, options || {});
  }
  async delete(key) {
    this.store.delete(key);
  }
}

async function createSignedWebhookRequest(bodyObj, secret, options = {}) {
  const rawBody = JSON.stringify(bodyObj);
  const webhookId = options.webhookId || `msg_${Date.now()}`;
  const webhookTimestamp = options.webhookTimestamp || Math.floor(Date.now() / 1000).toString();

  const signedContent = `${webhookId}.${webhookTimestamp}.${rawBody}`;
  const encoder = new TextEncoder();
  const signedContentBytes = encoder.encode(signedContent);

  let secretKeyBytes;
  if (secret.startsWith("whsec_")) {
    const binary = atob(secret.slice(6));
    secretKeyBytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      secretKeyBytes[i] = binary.charCodeAt(i);
    }
  } else {
    secretKeyBytes = encoder.encode(secret);
  }

  const cryptoKey = await crypto.subtle.importKey(
    "raw",
    secretKeyBytes,
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const sigBuf = await crypto.subtle.sign("HMAC", cryptoKey, signedContentBytes);
  const sigBytes = new Uint8Array(sigBuf);
  let binary = "";
  for (let i = 0; i < sigBytes.byteLength; i++) {
    binary += String.fromCharCode(sigBytes[i]);
  }
  const signatureBase64 = btoa(binary);

  const headers = new Headers({
    "Content-Type": "application/json",
    "webhook-id": webhookId,
    "webhook-timestamp": webhookTimestamp,
    "webhook-signature": `v1,${signatureBase64}`
  });

  return new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/webhook/dodo", {
    method: "POST",
    headers: headers,
    body: rawBody
  });
}

describe("Cipher License Worker Security & Endpoints", () => {
  let env;

  beforeEach(() => {
    env = {
      CIPHER_LICENSES: new MockKV(),
      DODO_WEBHOOK_SECRET: "whsec_dGVzdF9zZWNyZXRfa2V5XzEyMzQ1Njc4OTAxMjM0"
    };
  });

  it("1. rejects forged webhook without valid signature", async () => {
    const fakeRequest = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/webhook/dodo", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "webhook-id": "fake_id",
        "webhook-timestamp": Math.floor(Date.now() / 1000).toString(),
        "webhook-signature": "v1,invalid_forged_sig"
      },
      body: JSON.stringify({
        type: "payment.succeeded",
        data: { license_key: "FORGED-KEY-1234" }
      })
    });

    const res = await worker.fetch(fakeRequest, env, {});
    expect(res.status).toBe(401);
    const json = await res.json();
    expect(json.success).toBe(false);
    expect(await env.CIPHER_LICENSES.get("FORGED-KEY-1234")).toBeNull();
  });

  it("1b. refuses every webhook, signed or not, when no secret is configured", async () => {
    delete env.DODO_WEBHOOK_SECRET;
    const forged = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/webhook/dodo", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        type: "payment.succeeded",
        data: { license_key: "MINTED-WITHOUT-SECRET", product_id: "pdt_any" }
      })
    });

    const res = await worker.fetch(forged, env, {});

    expect(res.status).toBe(503);
    expect((await res.json()).success).toBe(false);
    expect(await env.CIPHER_LICENSES.get("MINTED-WITHOUT-SECRET")).toBeNull();
  });

  it("1c. a forged refund cannot revoke a license when no secret is configured", async () => {
    await env.CIPHER_LICENSES.put("REAL-KEY-1", JSON.stringify({ key: "REAL-KEY-1", tier: "LIFETIME", status: "ACTIVE", activatedDevices: [] }));
    delete env.DODO_WEBHOOK_SECRET;
    const forgedRefund = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/webhook/dodo", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ type: "refund.succeeded", data: { license_key: "REAL-KEY-1" } })
    });

    const res = await worker.fetch(forgedRefund, env, {});

    expect(res.status).toBe(503);
    expect((await env.CIPHER_LICENSES.get("REAL-KEY-1", { type: "json" })).status).toBe("ACTIVE");
  });

  it("2. accepts genuine signed webhook and maps product tier correctly", async () => {
    const payload = {
      type: "payment.succeeded",
      data: {
        payment_id: "pay_1001",
        license_key: "CIPHER-ANNUAL-TEST-KEY-1234",
        product_id: "pdt_0Nnvgcewj9JY1Oyrs2kWF",
        product_name: "Cipher Pro - 1 Year Annual Pass",
        customer: { email: "alice@example.com" }
      }
    };

    const req = await createSignedWebhookRequest(payload, env.DODO_WEBHOOK_SECRET);
    const res = await worker.fetch(req, env, {});
    expect(res.status).toBe(200);
    const json = await res.json();
    expect(json.success).toBe(true);
    expect(json.licenseKey).toBe("CIPHER-ANNUAL-TEST-KEY-1234");
    expect(json.tier).toBe("ANNUAL");

    const record = await env.CIPHER_LICENSES.get("CIPHER-ANNUAL-TEST-KEY-1234", { type: "json" });
    expect(record).not.toBeNull();
    expect(record.tier).toBe("ANNUAL");
    expect(record.email).toBe("alice@example.com");
  });

  it("3. handles duplicate webhook idempotently without wiping activated devices", async () => {
    const key = "CIPHER-LIFETIME-DUPLICATE-TEST";
    const payload = {
      type: "payment.succeeded",
      data: {
        payment_id: "pay_duplicate_1",
        license_key: key,
        product_id: "pdt_0NnvgqmXN76K14C90kLjX",
        customer: { email: "bob@example.com" }
      }
    };

    const req1 = await createSignedWebhookRequest(payload, env.DODO_WEBHOOK_SECRET);
    await worker.fetch(req1, env, {});

    const actReq = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/activate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ licenseKey: key, deviceId: "device_bob_phone" })
    });
    const actRes = await worker.fetch(actReq, env, {});
    expect(actRes.status).toBe(200);

    const req2 = await createSignedWebhookRequest(payload, env.DODO_WEBHOOK_SECRET);
    const res2 = await worker.fetch(req2, env, {});
    expect(res2.status).toBe(200);
    const json2 = await res2.json();
    expect(json2.idempotent).toBe(true);

    const record = await env.CIPHER_LICENSES.get(key, { type: "json" });
    expect(record.activatedDevices.length).toBe(1);
    expect(record.activatedDevices[0].deviceId).toBe("device_bob_phone");
  });

  it("4. flags missing Dodo license_key and does not grant silent key", async () => {
    const payload = {
      type: "payment.succeeded",
      data: {
        payment_id: "pay_missing_key_99",
        product_id: "pdt_0NnvgqmXN76K14C90kLjX",
        customer: { email: "charlie@example.com" }
      }
    };

    const req = await createSignedWebhookRequest(payload, env.DODO_WEBHOOK_SECRET);
    const res = await worker.fetch(req, env, {});
    expect(res.status).toBe(200);
    const json = await res.json();
    expect(json.success).toBe(false);
    expect(json.error).toContain("missing license_key");

    const missingLog = await env.CIPHER_LICENSES.get("MISSING_KEY:pay_missing_key_99", { type: "json" });
    expect(missingLog).not.toBeNull();
    expect(missingLog.email).toBe("charlie@example.com");
  });

  it("4b. never mints a record named after a non-string license_key", async () => {
    const payload = {
      type: "payment.succeeded",
      data: {
        payment_id: "pay_object_key_1",
        license_key: { id: "lic_1", status: "active" },
        product_id: "pdt_0NnvgqmXN76K14C90kLjX",
        customer: { email: "dave@example.com" }
      }
    };

    const res = await worker.fetch(await createSignedWebhookRequest(payload, env.DODO_WEBHOOK_SECRET), env, {});

    expect((await res.json()).success).toBe(false);
    expect(await env.CIPHER_LICENSES.get("[OBJECT OBJECT]")).toBeNull();
    expect(await env.CIPHER_LICENSES.get("MISSING_KEY:pay_object_key_1", { type: "json" })).not.toBeNull();
  });

  it("4c. reads the key out of a license_key object", async () => {
    const payload = {
      type: "payment.succeeded",
      data: {
        payment_id: "pay_object_key_2",
        license_key: { key: "a7576dcf-6ada-4c2d-941c-195879343030" },
        product_id: "pdt_0NnvgqmXN76K14C90kLjX",
        customer: { email: "erin@example.com" }
      }
    };

    const res = await worker.fetch(await createSignedWebhookRequest(payload, env.DODO_WEBHOOK_SECRET), env, {});

    expect((await res.json()).licenseKey).toBe("A7576DCF-6ADA-4C2D-941C-195879343030");
    expect(await env.CIPHER_LICENSES.get("A7576DCF-6ADA-4C2D-941C-195879343030", { type: "json" })).not.toBeNull();
  });

  it("4d. clears the missing-key flag once the key arrives for that payment", async () => {
    const withoutKey = { type: "payment.succeeded", data: { payment_id: "pay_late_key", product_id: "pdt_0NnvgqmXN76K14C90kLjX" } };
    await worker.fetch(await createSignedWebhookRequest(withoutKey, env.DODO_WEBHOOK_SECRET, { webhookId: "msg_a" }), env, {});
    expect(await env.CIPHER_LICENSES.get("MISSING_KEY:pay_late_key")).not.toBeNull();

    const withKey = { type: "license_key.created", data: { payment_id: "pay_late_key", license_key: "LATE-KEY-1", product_id: "pdt_0NnvgqmXN76K14C90kLjX" } };
    await worker.fetch(await createSignedWebhookRequest(withKey, env.DODO_WEBHOOK_SECRET, { webhookId: "msg_b" }), env, {});

    expect(await env.CIPHER_LICENSES.get("MISSING_KEY:pay_late_key")).toBeNull();
    expect(await env.CIPHER_LICENSES.get("LATE-KEY-1", { type: "json" })).not.toBeNull();
  });

  it("5. rejects unknown / forged license key on /api/activate", async () => {
    const req = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/activate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ licenseKey: "CIPHER-LIFETIME-UNKNOWN-9999", deviceId: "dev_1" })
    });

    const res = await worker.fetch(req, env, {});
    expect(res.status).toBe(404);
    const json = await res.json();
    expect(json.success).toBe(false);
  });

  it("6. enforces 3-device limit: activates 3 devices and rejects 4th device", async () => {
    const key = "CIPHER-LIFETIME-THREE-DEV";
    await env.CIPHER_LICENSES.put(key, JSON.stringify({
      key: key,
      tier: "LIFETIME",
      status: "ACTIVE",
      maxDevices: 3,
      activatedDevices: []
    }));

    for (let i = 1; i <= 3; i++) {
      const actReq = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/activate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ licenseKey: key, deviceId: `device_${i}`, deviceName: `Phone ${i}` })
      });
      const actRes = await worker.fetch(actReq, env, {});
      expect(actRes.status).toBe(200);
      const data = await actRes.json();
      expect(data.deviceCount).toBe(i);
    }

    const actReq4 = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/activate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ licenseKey: key, deviceId: "device_4", deviceName: "Phone 4" })
    });
    const actRes4 = await worker.fetch(actReq4, env, {});
    expect(actRes4.status).toBe(403);
    const data4 = await actRes4.json();
    expect(data4.success).toBe(false);
    expect(data4.error).toContain("Device limit reached");
  });

  it("7. allows device reactivation without counting as a new seat", async () => {
    const key = "CIPHER-LIFETIME-SEAT-TEST";
    await env.CIPHER_LICENSES.put(key, JSON.stringify({
      key: key,
      tier: "LIFETIME",
      status: "ACTIVE",
      maxDevices: 3,
      activatedDevices: [{ deviceId: "my_phone", deviceName: "Pixel 9", activatedAt: 1000, lastSeenAt: 1000 }]
    }));

    const req = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/activate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ licenseKey: key, deviceId: "my_phone", deviceName: "Pixel 9 Pro" })
    });
    const res = await worker.fetch(req, env, {});
    expect(res.status).toBe(200);
    const data = await res.json();
    expect(data.deviceCount).toBe(1);
    expect(data.message).toContain("restored");
  });

  it("8. revoking a device frees up a slot for a new device", async () => {
    const key = "CIPHER-LIFETIME-REVOKE-TEST";
    await env.CIPHER_LICENSES.put(key, JSON.stringify({
      key: key,
      tier: "LIFETIME",
      status: "ACTIVE",
      maxDevices: 3,
      activatedDevices: [
        { deviceId: "dev_1", deviceName: "Dev 1" },
        { deviceId: "dev_2", deviceName: "Dev 2" },
        { deviceId: "dev_3", deviceName: "Dev 3" }
      ]
    }));

    const revokeReq = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/revoke", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ licenseKey: key, deviceId: "dev_2" })
    });
    const revokeRes = await worker.fetch(revokeReq, env, {});
    expect(revokeRes.status).toBe(200);
    const revokeData = await revokeRes.json();
    expect(revokeData.deviceCount).toBe(2);

    const actReq4 = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/activate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ licenseKey: key, deviceId: "dev_4", deviceName: "Dev 4" })
    });
    const actRes4 = await worker.fetch(actReq4, env, {});
    expect(actRes4.status).toBe(200);
    const actData4 = await actRes4.json();
    expect(actData4.deviceCount).toBe(3);
  });

  it("9. refund / dispute revokes the license and blocks subsequent activations", async () => {
    const key = "CIPHER-LIFETIME-REFUND-TEST";
    await env.CIPHER_LICENSES.put(key, JSON.stringify({
      key: key,
      tier: "LIFETIME",
      status: "ACTIVE",
      maxDevices: 3,
      activatedDevices: [{ deviceId: "dev_1" }]
    }));

    const refundPayload = {
      type: "refund.succeeded",
      data: {
        payment_id: "pay_refund_1",
        license_key: key
      }
    };

    const req = await createSignedWebhookRequest(refundPayload, env.DODO_WEBHOOK_SECRET);
    const res = await worker.fetch(req, env, {});
    expect(res.status).toBe(200);

    const record = await env.CIPHER_LICENSES.get(key, { type: "json" });
    expect(record.status).toBe("REVOKED");

    const actReq = new Request("https://cipher-license-api.skmasumali-main.workers.dev/api/activate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ licenseKey: key, deviceId: "dev_new" })
    });
    const actRes = await worker.fetch(actReq, env, {});
    expect(actRes.status).toBe(403);
    const actData = await actRes.json();
    expect(actData.error).toContain("revoked");
  });

  const MONTHLY_PRODUCT = "pdt_0NnvfQxm1f1vLtVQvAiYW";
  const daysFromNow = (days) => new Date(Date.now() + days * 86400000).toISOString();
  let webhookCounter = 0;

  async function sendEvent(type, data) {
    webhookCounter += 1;
    const req = await createSignedWebhookRequest({ type, data }, env.DODO_WEBHOOK_SECRET, { webhookId: `msg_sub_${webhookCounter}` });
    return worker.fetch(req, env, {});
  }

  async function createSubscriptionKey(overrides = {}) {
    await sendEvent("license_key.created", {
      key: "dac89841-7918-4eb1-9e08-c7a3a37dfd07",
      payment_id: "pay_sub_1",
      subscription_id: "sub_1",
      product_id: MONTHLY_PRODUCT,
      status: "active",
      ...overrides
    });
    return "DAC89841-7918-4EB1-9E08-C7A3A37DFD07";
  }

  async function fetchDevices(key) {
    const res = await worker.fetch(new Request(`https://cipher-license-api.skmasumali-main.workers.dev/api/devices?licenseKey=${key}&deviceId=d1`), env, {});
    return { status: res.status, json: await res.json() };
  }

  it("10. license_key.created from Dodo creates a record and indexes its subscription", async () => {
    const key = await createSubscriptionKey();

    const record = await env.CIPHER_LICENSES.get(key, { type: "json" });
    expect(record.tier).toBe("MONTHLY");
    expect(record.status).toBe("ACTIVE");
    expect(await env.CIPHER_LICENSES.get("SUB:sub_1")).toBe(key);
    expect(await env.CIPHER_LICENSES.get("ORDER:pay_sub_1")).toBe(key);
  });

  it("11. a cancellation with no paid time left expires the key via the subscription index", async () => {
    const key = await createSubscriptionKey();

    await sendEvent("subscription.cancelled", {
      subscription_id: "sub_1",
      status: "cancelled",
      cancel_at_next_billing_date: false,
      next_billing_date: daysFromNow(-1)
    });

    const { status, json } = await fetchDevices(key);
    expect(status).toBe(403);
    expect(json.status).toBe("EXPIRED");
  });

  it("12. cancelling never cuts off paid time, however the cancellation was made", async () => {
    const key = await createSubscriptionKey();
    const nextBilling = daysFromNow(20);

    await sendEvent("subscription.cancelled", {
      subscription_id: "sub_1",
      status: "cancelled",
      cancel_at_next_billing_date: false,
      next_billing_date: nextBilling
    });

    const { status, json } = await fetchDevices(key);
    expect(status).toBe(200);
    expect(json.expiresAt).toBe(Date.parse(nextBilling));
  });

  it("12b. a period-end cancellation also keeps the key active and reports the date", async () => {
    const key = await createSubscriptionKey();
    const nextBilling = daysFromNow(20);

    await sendEvent("subscription.cancelled", {
      subscription_id: "sub_1",
      status: "cancelled",
      cancel_at_next_billing_date: true,
      next_billing_date: nextBilling
    });

    const { status, json } = await fetchDevices(key);
    expect(status).toBe(200);
    expect(json.expiresAt).toBe(Date.parse(nextBilling));
  });

  it("13. a renewal moves the expiry forward and a lapsed key stops working after the grace period", async () => {
    const key = await createSubscriptionKey();
    await sendEvent("subscription.active", { subscription_id: "sub_1", status: "active", next_billing_date: daysFromNow(30) });
    expect((await fetchDevices(key)).json.expiresAt).toBeGreaterThan(Date.now() + 29 * 86400000);

    const record = await env.CIPHER_LICENSES.get(key, { type: "json" });
    record.expiresAt = Date.now() - 2 * 86400000;
    await env.CIPHER_LICENSES.put(key, JSON.stringify(record));
    expect((await fetchDevices(key)).status).toBe(200);

    record.expiresAt = Date.now() - 4 * 86400000;
    await env.CIPHER_LICENSES.put(key, JSON.stringify(record));
    const lapsed = await fetchDevices(key);
    expect(lapsed.status).toBe(403);
    expect(lapsed.json.status).toBe("EXPIRED");
  });

  it("14. a subscription event that arrives before its key is applied once the key is created", async () => {
    await sendEvent("subscription.active", { subscription_id: "sub_1", status: "active", next_billing_date: daysFromNow(30) });
    const key = await createSubscriptionKey();

    const { json } = await fetchDevices(key);
    expect(json.expiresAt).toBeGreaterThan(Date.now() + 29 * 86400000);
    expect(await env.CIPHER_LICENSES.get("SUBSCRIPTION_PENDING:sub_1")).toBeNull();
  });

  it("15. a refund that only carries the payment id revokes the license through the order index", async () => {
    const key = await createSubscriptionKey();

    await sendEvent("refund.succeeded", { payment_id: "pay_sub_1" });

    expect((await env.CIPHER_LICENSES.get(key, { type: "json" })).status).toBe("REVOKED");
  });

  it("16. replaying the key creation event never revives an expired or revoked key", async () => {
    const key = await createSubscriptionKey();
    await sendEvent("subscription.cancelled", { subscription_id: "sub_1", status: "cancelled", cancel_at_next_billing_date: false });

    await createSubscriptionKey();

    expect((await env.CIPHER_LICENSES.get(key, { type: "json" })).status).toBe("EXPIRED");
  });

  it("17. events that never carry a key do not raise missing-key alarms", async () => {
    const res = await sendEvent("entitlement_grant.created", { id: "ent_1", customer_id: "cus_1" });

    expect((await res.json()).ignored).toBe(true);
    expect(await env.CIPHER_LICENSES.get("MISSING_KEY:ent_1")).toBeNull();
  });

  it("18. temporary alarm and pending entries expire on their own", async () => {
    await sendEvent("payment.succeeded", { payment_id: "pay_no_key_ttl", product_id: MONTHLY_PRODUCT });
    await sendEvent("subscription.active", { subscription_id: "sub_ttl", status: "active", next_billing_date: daysFromNow(30) });

    expect(env.CIPHER_LICENSES.options.get("MISSING_KEY:pay_no_key_ttl").expirationTtl).toBe(30 * 24 * 60 * 60);
    expect(env.CIPHER_LICENSES.options.get("SUBSCRIPTION_PENDING:sub_ttl").expirationTtl).toBe(7 * 24 * 60 * 60);
  });
});
