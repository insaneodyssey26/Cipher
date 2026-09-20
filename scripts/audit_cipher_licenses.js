#!/usr/bin/env node

const https = require("https");

const ACCOUNT_ID = process.env.CF_ACCOUNT_ID;
const NAMESPACE_ID = process.env.CF_KV_NAMESPACE_ID;
const API_TOKEN = process.env.CF_API_TOKEN || process.env.CLOUDFLARE_API_TOKEN;

if (!ACCOUNT_ID || !NAMESPACE_ID || !API_TOKEN) {
  console.error("Missing required environment variables.");
  console.error("Please provide: CF_ACCOUNT_ID, CF_KV_NAMESPACE_ID, CF_API_TOKEN");
  console.error("Example:");
  console.error("  CF_ACCOUNT_ID=\"xxx\" CF_KV_NAMESPACE_ID=\"yyy\" CF_API_TOKEN=\"zzz\" node scripts/audit_cipher_licenses.js");
  process.exit(1);
}

function cfApiGet(path) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: "api.cloudflare.com",
      port: 443,
      path: path,
      method: "GET",
      headers: {
        "Authorization": `Bearer ${API_TOKEN}`,
        "Content-Type": "application/json"
      }
    };

    const req = https.request(options, (res) => {
      let data = "";
      res.on("data", (chunk) => { data += chunk; });
      res.on("end", () => {
        try {
          const parsed = JSON.parse(data);
          resolve(parsed);
        } catch {
          resolve(data);
        }
      });
    });

    req.on("error", (e) => reject(e));
    req.end();
  });
}

async function listAllKeys() {
  let allKeys = [];
  let cursor = null;

  do {
    const query = cursor ? `?cursor=${encodeURIComponent(cursor)}&limit=1000` : "?limit=1000";
    const path = `/client/v4/accounts/${ACCOUNT_ID}/storage/kv/namespaces/${NAMESPACE_ID}/keys${query}`;
    const res = await cfApiGet(path);

    if (!res.success) {
      throw new Error(`Failed to list keys: ${JSON.stringify(res.errors || res)}`);
    }

    allKeys = allKeys.concat(res.result || []);
    cursor = res.result_info?.cursor;
  } while (cursor);

  return allKeys;
}

async function getKeyValue(keyName) {
  const path = `/client/v4/accounts/${ACCOUNT_ID}/storage/kv/namespaces/${NAMESPACE_ID}/values/${encodeURIComponent(keyName)}`;
  return cfApiGet(path);
}

async function runAudit() {
  console.log("Fetching all keys from CIPHER_LICENSES KV...");
  const keys = await listAllKeys();
  console.log(`Total KV entries found: ${keys.length}\n`);

  const orderKeys = new Set(
    keys
      .map(k => k.name)
      .filter(name => name.startsWith("ORDER:"))
  );

  const licenseKeys = keys
    .map(k => k.name)
    .filter(name => !name.startsWith("ORDER:") && !name.startsWith("EMAIL:") && !name.startsWith("MISSING_KEY:"));

  const results = [];

  for (const key of licenseKeys) {
    const rawVal = await getKeyValue(key);
    let record = rawVal;
    if (typeof rawVal === "string") {
      try {
        record = JSON.parse(rawVal);
      } catch {
        record = { raw: rawVal };
      }
    }

    const orderId = record.orderId || null;
    const hasMatchingOrder = orderId ? orderKeys.has(`ORDER:${orderId}`) : false;

    let source = "UNKNOWN";
    if (record.orderId && !record.firstActivatedAt) {
      source = "DODO_WEBHOOK";
    } else if (record.firstActivatedAt && !record.orderId) {
      source = "LAZY_APP_ACTIVATION (Checksum/Promo)";
    } else if (record.orderId && record.firstActivatedAt) {
      source = "WEBHOOK_THEN_ACTIVATED";
    } else {
      source = "MANUAL_OR_LEGACY";
    }

    const keyPrefix = key.split("-").slice(0, 2).join("-");
    const activeDevs = Array.isArray(record.activatedDevices) ? record.activatedDevices.length : 0;
    const createdAtStr = record.createdAt ? new Date(record.createdAt).toISOString() : (record.firstActivatedAt ? new Date(record.firstActivatedAt).toISOString() : "N/A");

    results.push({
      licenseKey: key,
      keyPrefix: keyPrefix,
      tier: record.tier || "UNSPECIFIED",
      status: record.status || (record.key ? "ACTIVE" : "UNSPECIFIED"),
      createdAt: createdAtStr,
      source: source,
      email: record.email || "NONE",
      orderId: orderId || "NONE",
      activatedDevices: activeDevs,
      maxDevices: record.maxDevices || 3,
      hasMatchingOrderEntry: hasMatchingOrder,
      flagged: !hasMatchingOrder && source !== "LAZY_APP_ACTIVATION (Checksum/Promo)"
    });
  }

  console.log("=".repeat(120));
  console.log("CIPHER_LICENSES KV AUDIT REPORT (READ-ONLY)");
  console.log("=".repeat(120));

  console.table(results.map(r => ({
    "Key Prefix": r.keyPrefix,
    "Tier": r.tier,
    "Status": r.status,
    "Source": r.source,
    "Email": r.email,
    "Order ID": r.orderId,
    "Devices": `${r.activatedDevices}/${r.maxDevices}`,
    "Order Key Exists": r.hasMatchingOrderEntry ? "YES" : "NO",
    "Flag": r.flagged ? "⚠️ NO ORDER ENTRY" : (r.source.includes("LAZY") ? "⚠️ FORGED/OFFLINE KEY" : "OK")
  })));

  console.log("\nAudit Summary:");
  console.log(`- Total Licenses: ${results.length}`);
  console.log(`- Sourced from Dodo Webhooks: ${results.filter(r => r.source.includes("WEBHOOK")).length}`);
  console.log(`- Sourced from Lazy/Checksum: ${results.filter(r => r.source.includes("LAZY")).length}`);
  console.log(`- Missing ORDER: index: ${results.filter(r => r.flagged).length}`);
}

runAudit().catch(err => {
  console.error("Audit failed:", err);
  process.exit(1);
});
