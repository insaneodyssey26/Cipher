#!/usr/bin/env python3
import hashlib
import sys
import time

SECRET_SALT = "CIPHER_VAULT_PRO_SECURE_SALT_2026"

def compute_check_code(input_str: str) -> str:
    h = hashlib.sha256((input_str + SECRET_SALT).encode('utf-8')).hexdigest()
    return h[:4].upper()

def generate_key(prefix: str = "CIPHER-LIFETIME", identifier: str = None) -> str:
    if not identifier:
        rand_hex = hashlib.sha256(str(time.time_ns()).encode('utf-8')).hexdigest()[:8].upper()
        identifier = rand_hex
    else:
        identifier = identifier.replace("-", "").upper()[:8]
        if len(identifier) < 8:
            identifier = identifier.ljust(8, 'X')
    
    checksum = compute_check_code(identifier)
    return f"{prefix}-{identifier[:4]}-{identifier[4:8]}-{checksum}"

def main():
    print("==========================================")
    print("      CIPHER PRO OFFLINE KEY GENERATOR    ")
    print("==========================================")
    
    if len(sys.argv) > 1 and sys.argv[1] == "--batch":
        count = int(sys.argv[2]) if len(sys.argv) > 2 else 10
        print(f"Generating {count} Lifetime Promo Keys:\n")
        for i in range(1, count + 1):
            key = generate_key(prefix="CIPHER-VIP")
            print(f"{i:02d}. {key}")
        return

    print("\n[1] Lifetime VIP Key:")
    print("   ", generate_key(prefix="CIPHER-VIP"))
    
    print("\n[2] Standard Lifetime Key:")
    print("   ", generate_key(prefix="CIPHER-LIFETIME"))
    
    print("\n[3] Early Bird Promo Key:")
    print("   ", generate_key(prefix="CIPHER-EARLY"))
    print("\n==========================================")

if __name__ == "__main__":
    main()
