#!/usr/bin/env python3
"""Bam mat khau theo dung cach backend hash (Shiro Md5Hash).

Dung: python3 hash_shiro.py <user_id> <mat_khau>  -> in ra hex hash.
Thuat toan (org.apache.shiro.crypto.hash.SimpleHash, salt = user id,
10000 vong — khop tools/hash/HashPwd.java cua backend):
  d = md5(salt_bytes + password_bytes)
  lap lai md5(d) them 9999 lan, tra ve hex.
Salt va password deu encode UTF-8.
"""
import hashlib
import sys


def shiro_md5(password, salt, iterations=10000):
    d = hashlib.md5(salt.encode("utf-8") + password.encode("utf-8")).digest()
    for _ in range(iterations - 1):
        d = hashlib.md5(d).digest()
    return d.hex()


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("dung: hash_shiro.py <user_id> <mat_khau>", file=sys.stderr)
        sys.exit(2)
    print(shiro_md5(sys.argv[2], sys.argv[1]))
