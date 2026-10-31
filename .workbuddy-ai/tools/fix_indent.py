#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Khôi phục thụt lề đầu dòng bị script chuẩn hoá dấu câu làm hỏng.

Nguyên tắc: đối chiếu tuần tự từng dòng hiện tại với bản trong git HEAD.
Nếu nội dung (sau khi gộp khoảng trắng) trùng nhau thì lấy lại phần
thụt lề đầu dòng của bản HEAD. Không đụng tới nội dung.
"""
import io
import os
import re
import subprocess
import sys

ROOT = 'Code/medical2.0/client/src'
WS = re.compile(r'[ \t]+')

# Cùng bảng ánh xạ dấu câu đã dùng khi chuẩn hoá, để so khớp được với HEAD
PUNCT = [
    (re.compile(r'\uff0c[ \t]*'), ', '),
    (re.compile(r'\u3002'), '.'),
    (re.compile(r'\uff01'), '!'),
    (re.compile(r'\uff1a[ \t]*'), ': '),
    (re.compile(r'\uff08'), ' ('),
    (re.compile(r'\uff09[ \t]*'), ')'),
    (re.compile(r'\uff1b[ \t]*'), '; '),
    (re.compile(r'\u3001[ \t]*'), ', '),
]


def collapse(line):
    """Chuẩn hoá khoảng trắng + dấu câu để so khớp nội dung hai phiên bản."""
    for pat, rep in PUNCT:
        line = pat.sub(rep, line)
    return WS.sub(' ', line).strip()


def head_lines(path):
    try:
        out = subprocess.check_output(
            ['git', 'show', 'HEAD:%s' % path], stderr=subprocess.DEVNULL)
        return out.decode('utf-8', 'replace').split('\n')
    except subprocess.CalledProcessError:
        return None


def indent_of(line):
    return line[:len(line) - len(line.lstrip(' \t'))]


def main():
    files = subprocess.check_output(
        ['git', 'status', '--porcelain'], cwd='.').decode('utf-8').split('\n')
    targets = []
    for row in files:
        if not row.strip():
            continue
        status, path = row[:2], row[3:].strip()
        if path.startswith('"') and path.endswith('"'):
            path = path[1:-1]
        if not path.startswith(ROOT):
            continue
        if not path.endswith(('.vue', '.js')):
            continue
        targets.append(path)

    fixed_total = 0
    for path in targets:
        hl = head_lines(path)
        if hl is None:
            continue
        with io.open(path, encoding='utf-8') as f:
            cur = f.read().split('\n')

        i = j = 0
        fixed = 0
        while i < len(hl) and j < len(cur):
            if collapse(cur[j]) == collapse(hl[i]):
                want = indent_of(hl[i])
                have = indent_of(cur[j])
                if want != have and len(want) > len(have):
                    cur[j] = want + cur[j].lstrip(' \t')
                    fixed += 1
                i += 1
                j += 1
            elif i + 1 < len(hl) and collapse(cur[j]) == collapse(hl[i + 1]):
                i += 2  # dòng trong HEAD đã bị xoá
            elif j + 1 < len(cur) and collapse(cur[j + 1]) == collapse(hl[i]):
                j += 2  # dòng mới được chèn
            else:
                i += 1
                j += 1

        if fixed:
            with io.open(path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(cur))
            fixed_total += fixed
            print('%4d  %s' % (fixed, os.path.relpath(path, ROOT)))

    print('\nTong so dong da khoi phuc thut le: %d' % fixed_total)


if __name__ == '__main__':
    sys.exit(main())
