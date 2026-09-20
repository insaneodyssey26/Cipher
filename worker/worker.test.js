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
  async put(key, val) {
    this.store.set(key, typeof val === "string" ? val : JSON.stringify(val));
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
      DODO_WEBHOOK_SECRET: "whsec_MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE"
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
});
