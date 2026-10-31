#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Quét ký tự Trung Quốc trong codebase frontend.
Phân loại:
  - TEMPLATE  : nằm trong khối <template> của file .vue  -> text người dùng nhìn thấy
  - SCRIPT-STR: nằm trong string literal ở khối <script> -> có thể là message hiển thị
  - COMMENT   : nằm trong comment (//, /*, <!--)         -> chỉ là ghi chú
  - OTHER     : còn lại (css content, v.v.)
"""
import os
import re
import sys
import json

CJK = re.compile(r'[\u4e00-\u9fff\u3400-\u4dbf]')

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'

SKIP_DIRS = {'node_modules', '.git', 'dist', 'lib', '__tests__'}

def strip_comments_js(line):
    """Trả về (code_part, comment_part) cho 1 dòng JS."""
    # comment dạng // (không tính // nằm trong chuỗi — xử lý đơn giản)
    m = re.search(r'(?<![:/])//', line)
    if m:
        return line[:m.start()], line[m.start():]
    return line, ''

def analyze_vue(path):
    with open(path, encoding='utf-8', errors='replace') as f:
        content = f.read()
    lines = content.split('\n')

    # xác định vùng template / script / style
    regions = []  # (start_line_idx, end_line_idx, kind)
    for tag in ('template', 'script', 'style'):
        for m in re.finditer(r'<%s\b[^>]*>(.*?)</%s>' % (tag, tag), content, re.S):
            s = content[:m.start(1)].count('\n')
            e = content[:m.end(1)].count('\n')
            regions.append((s, e, tag))
    regions.sort()

    def kind_of(i):
        for s, e, k in regions:
            if s <= i <= e:
                return k
        return 'other'

    out = []
    for i, line in enumerate(lines):
        if not CJK.search(line):
            continue
        region = kind_of(i)
        stripped = line.strip()

        if region == 'template':
            # comment HTML
            if stripped.startswith('<!--') or '<!--' in line:
                out.append((i + 1, 'COMMENT', line.strip()))
            else:
                out.append((i + 1, 'TEMPLATE', line.strip()))
        elif region == 'script':
            code, comment = strip_comments_js(line)
            if stripped.startswith('//') or stripped.startswith('*') or stripped.startswith('/*'):
                out.append((i + 1, 'COMMENT', line.strip()))
            elif CJK.search(code):
                out.append((i + 1, 'SCRIPT-STR', line.strip()))
            else:
                out.append((i + 1, 'COMMENT', line.strip()))
        elif region == 'style':
            if stripped.startswith('/*') or stripped.startswith('//') or stripped.startswith('*'):
                out.append((i + 1, 'COMMENT', line.strip()))
            else:
                out.append((i + 1, 'OTHER', line.strip()))
        else:
            out.append((i + 1, 'OTHER', line.strip()))
    return out

def analyze_plain(path):
    out = []
    with open(path, encoding='utf-8', errors='replace') as f:
        for i, line in enumerate(f):
            if not CJK.search(line):
                continue
            s = line.strip()
            if s.startswith('//') or s.startswith('*') or s.startswith('/*') or s.startswith('#'):
                out.append((i + 1, 'COMMENT', s))
                continue
            code, comment = strip_comments_js(line)
            if CJK.search(code):
                out.append((i + 1, 'SCRIPT-STR', s))
            else:
                out.append((i + 1, 'COMMENT', s))
    return out

def main():
    result = {}
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for fn in filenames:
            if not fn.endswith(('.vue', '.js', '.scss', '.css')):
                continue
            p = os.path.join(dirpath, fn)
            try:
                items = analyze_vue(p) if fn.endswith('.vue') else analyze_plain(p)
            except Exception as e:
                continue
            if items:
                rel = os.path.relpath(p, ROOT)
                result[rel] = items

    # tổng hợp theo loại
    summary = {'TEMPLATE': 0, 'SCRIPT-STR': 0, 'COMMENT': 0, 'OTHER': 0}
    for rel, items in result.items():
        for ln, kind, txt in items:
            summary[kind] = summary.get(kind, 0) + 1

    print('=== TONG HOP ===')
    for k, v in summary.items():
        print('%-12s %d' % (k, v))
    print()

    # in các file có text người dùng thấy
    print('=== FILE CO TEXT HIEN THI (TEMPLATE / SCRIPT-STR) ===')
    for rel in sorted(result):
        hits = [x for x in result[rel] if x[1] in ('TEMPLATE', 'SCRIPT-STR')]
        if hits:
            print('\n### %s  (%d)' % (rel, len(hits)))
            for ln, kind, txt in hits[:60]:
                print('  %5d [%s] %s' % (ln, kind, txt[:160]))

    with open('.workbuddy-ai/tools/cn_report.json', 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=1)

if __name__ == '__main__':
    main()
