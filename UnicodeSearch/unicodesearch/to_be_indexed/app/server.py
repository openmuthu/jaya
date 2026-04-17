#!/usr/bin/env python3
"""
Jaya Grantha Viewer — Python HTTP server
Usage: python3 server.py [port]   (default port: 3000)
"""

import json
import os
import re
import sys
import urllib.parse
from functools import lru_cache
from http.server import BaseHTTPRequestHandler, HTTPServer

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
APP_DIR  = os.path.abspath(os.path.dirname(__file__))
PORT     = int(sys.argv[1]) if len(sys.argv) > 1 else 3000

MIME = {
    '.html': 'text/html; charset=utf-8',
    '.css':  'text/css',
    '.js':   'application/javascript',
    '.json': 'application/json',
    '.ico':  'image/x-icon',
    '.png':  'image/png',
    '.svg':  'image/svg+xml',
}

# ── Security ──────────────────────────────────────────────────────────────────

def safe_path(rel: str):
    """Return absolute path inside BASE_DIR, or None if forbidden."""
    abs_path = os.path.normpath(os.path.join(BASE_DIR, rel.lstrip('/')))
    if not abs_path.startswith(BASE_DIR + os.sep) and abs_path != BASE_DIR:
        return None
    if abs_path.startswith(APP_DIR):
        return None
    return abs_path

# ── File reading ──────────────────────────────────────────────────────────────

@lru_cache(maxsize=50)
def read_text_file(abs_path: str) -> str:
    with open(abs_path, 'rb') as f:
        raw = f.read()
    # UTF-16 with BOM
    if raw[:2] in (b'\xff\xfe', b'\xfe\xff'):
        return raw.decode('utf-16')
    # Try UTF-16 LE (most files here)
    try:
        return raw.decode('utf-16-le')
    except Exception:
        return raw.decode('utf-8', errors='replace')

# ── Tree builder ──────────────────────────────────────────────────────────────

def build_tree(directory: str, rel_path: str) -> dict:
    try:
        entries = os.scandir(directory)
    except PermissionError:
        return None

    children = []
    for entry in sorted(entries, key=lambda e: (not e.is_dir(), e.name.lower())):
        if entry.name.startswith('.'):
            continue
        if rel_path == '' and entry.name == 'app':
            continue
        child_rel = f"{rel_path}/{entry.name}" if rel_path else entry.name
        if entry.is_dir():
            subtree = build_tree(entry.path, child_rel)
            if subtree:
                children.append(subtree)
        elif entry.name.endswith('.txt'):
            children.append({'name': entry.name, 'path': child_rel, 'type': 'file'})

    return {
        'name': os.path.basename(directory) or rel_path,
        'path': rel_path,
        'type': 'dir',
        'children': children
    }

# ── Collect all .txt files ────────────────────────────────────────────────────

def collect_files(directory: str, rel_path: str, result: list):
    try:
        entries = os.scandir(directory)
    except PermissionError:
        return
    for entry in entries:
        if entry.name.startswith('.'):
            continue
        if rel_path == '' and entry.name == 'app':
            continue
        child_rel = f"{rel_path}/{entry.name}" if rel_path else entry.name
        if entry.is_dir():
            collect_files(entry.path, child_rel, result)
        elif entry.name.endswith('.txt'):
            result.append(child_rel)

# ── ITRANS → Devanagari (for search normalisation) ───────────────────────────

ITRANS_TO_DEVA = {
    'a':'\u0905','A':'\u0906','aa':'\u0906',
    'i':'\u0907','ii':'\u0908','I':'\u0908',
    'u':'\u0909','U':'\u090A','uu':'\u090A',
    'R':'\u090B','Ri':'\u090B','Ru':'\u090B','RRi':'\u090B','R^i':'\u090B',
    'RR':'\u0960','RRI':'\u0960','R^I':'\u0960',
    'LLi':'\u090C','L^i':'\u090C','LLI':'\u0961','L^I':'\u0961',
    'e':'\u090E','E':'\u090F','ai':'\u0910',
    'o':'\u0912','O':'\u0913','au':'\u0914','ou':'\u0914',
    'M':'\u0902','H':'\u0903',':':'\u0903',
    'k':'\u0915','K':'\u0916','kh':'\u0916','Kh':'\u0916',
    'g':'\u0917','G':'\u0918','gh':'\u0918','Gh':'\u0918',
    '~N':'\u0919',
    'c':'\u091A','ch':'\u091A','C':'\u091B','Ch':'\u091B',
    'j':'\u091C','J':'\u091D','jh':'\u091D','Jh':'\u091D',
    '~n':'\u091E','jn':'\u091C\u094D\u091E',
    'T':'\u091F','Th':'\u0920','D':'\u0921','Dh':'\u0922','N':'\u0923',
    't':'\u0924','th':'\u0925','d':'\u0926','dh':'\u0927','n':'\u0928',
    'p':'\u092A','P':'\u092B','ph':'\u092B','Ph':'\u092B',
    'b':'\u092C','B':'\u092D','bh':'\u092D','Bh':'\u092D',
    'm':'\u092E','y':'\u092F','r':'\u0930','l':'\u0932','L':'\u0933',
    'v':'\u0935','w':'\u0935',
    'sh':'\u0936','Sh':'\u0937','S':'\u0937','s':'\u0938','h':'\u0939',
    'x':'\u0915\u094D\u0937','OM':'\u0950','.a':'\u093D','.':'.'
}

def _is_deva_cons(ch):  return 0x0915 <= ord(ch) <= 0x0939
def _is_deva_vowel(ch): return 0x0905 <= ord(ch) <= 0x0914

def itrans_to_devanagari(text: str) -> str:
    result = []
    pos = 0
    n = len(text)
    prev_cons = False
    while pos < n:
        target = None
        match_len = 0
        for l in range(min(4, n - pos), 0, -1):
            key = text[pos:pos+l]
            if key in ITRANS_TO_DEVA:
                target = ITRANS_TO_DEVA[key]
                match_len = l
                break
        if target is None:
            if prev_cons:
                result.append('\u094D')
                prev_cons = False
            result.append(text[pos])
            pos += 1
            continue
        first = target[0]
        is_cons  = _is_deva_cons(first)
        is_vowel = _is_deva_vowel(first)
        skip = False
        if prev_cons:
            if is_cons:
                result.append('\u094D')
            elif is_vowel:
                cp = ord(first)
                if cp != 0x0905:
                    target = chr(cp + 56)
                else:
                    skip = True
        prev_cons = is_cons
        if not skip:
            result.append(target)
        pos += match_len
    if prev_cons:
        result.append('\u094D')
    return ''.join(result)

def kannada_to_devanagari(text: str) -> str:
    return ''.join(chr(ord(c) - 0x0300) if 0x0C00 <= ord(c) <= 0x0CFF else c for c in text)

def telugu_to_devanagari(text: str) -> str:
    return ''.join(chr(ord(c) - 0x0300) if 0x0C00 <= ord(c) <= 0x0C7F else c for c in text)

def normalize_to_devanagari(query: str, script: str) -> str:
    if script == 'itrans':   return itrans_to_devanagari(query)
    if script == 'kannada':  return kannada_to_devanagari(query)
    if script == 'telugu':   return telugu_to_devanagari(query)
    return query

# ── Levenshtein (fuzzy) ───────────────────────────────────────────────────────

def levenshtein(a: str, b: str) -> int:
    if a == b: return 0
    m, n = len(a), len(b)
    if m == 0: return n
    if n == 0: return m
    prev = list(range(n + 1))
    for i in range(1, m + 1):
        curr = [i] + [0] * n
        for j in range(1, n + 1):
            curr[j] = prev[j-1] if a[i-1] == b[j-1] else 1 + min(prev[j], curr[j-1], prev[j-1])
        prev = curr
    return prev[n]

# ── Search ────────────────────────────────────────────────────────────────────

def do_search(q: str, script: str, paths_filter: list, fuzzy: bool, regex_mode: bool,
              offset: int, limit: int) -> dict:
    deva_query = normalize_to_devanagari(q, script)

    # Build matcher
    if regex_mode:
        try:
            pat = re.compile(deva_query, re.IGNORECASE)
            match_fn = lambda line: bool(pat.search(line))
        except re.error as e:
            return {'error': f'Invalid regex: {e}', 'results': [], 'total': 0}
    elif fuzzy:
        words = deva_query.split()
        def match_fn(line):
            for w in words:
                if w in line: continue
                found = False
                threshold = max(1, len(w) // 4)
                for i in range(max(0, len(line) - len(w) + 1)):
                    if levenshtein(w, line[i:i+len(w)]) <= threshold:
                        found = True
                        break
                if not found: return False
            return True
    else:
        match_fn = lambda line: deva_query in line

    # Collect file list
    all_files = []
    collect_files(BASE_DIR, '', all_files)

    if paths_filter:
        all_files = [f for f in all_files
                     if any(f == p or f.startswith(p + '/') for p in paths_filter)]

    results = []
    for rel_file in all_files:
        abs_f = safe_path(rel_file)
        if not abs_f:
            continue
        try:
            content = read_text_file(abs_f)
        except Exception:
            continue
        lines = content.splitlines()
        for idx, line in enumerate(lines):
            stripped = line.strip()
            if not stripped:
                continue
            if match_fn(stripped):
                results.append({
                    'file':       rel_file,
                    'lineNumber': idx + 1,
                    'lineText':   stripped,
                })
        if len(results) > 2000:
            break

    total = len(results)
    page  = results[offset:offset + limit]
    return {'results': page, 'total': total, 'query': q, 'devaQuery': deva_query}

# ── Outline / verse-navigation builder ───────────────────────────────────────

# Devanagari digit → ASCII digit
_DEVA_DIGIT = str.maketrans('०१२३४५६७८९', '0123456789')

# Matches verse-number stamps in several formats:
#   ॥ 1.2.3 ॥   ॥1॥   ॥ 12 ॥   ॥ _1 ॥   । । 1   ॥1.2.3॥
_RE_STAMP = re.compile(
    r'(?:'
    r'॥\s*_?\s*((?:[0-9०-९]+\.)*[0-9०-९]+)\s*(?:॥|$)'  # ॥...॥ or ॥... (line end)
    r'|'
    r'।\s*।\s*((?:[0-9०-९]+\.)*[0-9०-९]+)\s*$'          # । । n at line end
    r')'
)

# Matches x.y or x.y.z anywhere on a line (e.g.  "1.1.1" used at line end)
_RE_DOTTED = re.compile(
    r'(?<!\d)((?:[0-9०-९]+\.){1,3}[0-9०-९]+)(?!\d)'
)

# Section/chapter header: a line that starts with "1. " or "१. " (number-dot-space)
_RE_SECTION = re.compile(
    r'^([0-9०-९]+)\.\s+(.+)$'
)


def _norm(s: str) -> str:
    """Normalise Devanagari digits to ASCII in a numeral string."""
    return s.translate(_DEVA_DIGIT)


def build_outline(text: str) -> dict:
    """
    Parse the text and return a nested outline suitable for a nav tree.

    Two strategies are tried in order:
      A) Dotted verses (x.y.z) — Veda-style files.
         Group by first component, then second, producing a 2-3 level tree.
      B) Section headers (number. Title) + simple verse stamps ॥n॥.
         Group verses under their nearest preceding section header.

    Return value:
      {
        "style": "dotted" | "sectioned" | "flat",
        "nodes": [
          { "label": "1", "lineNumber": 7, "level": 0,
            "children": [
              { "label": "1.1", "lineNumber": 7, "level": 1,
                "children": [
                  { "label": "1.1.1", "lineNumber": 7, "level": 2 }
                ]}]}]}
    """
    lines = text.splitlines()

    # ── Strategy A: dotted verses ─────────────────────────────────────────────
    dotted_entries = []          # (lineNo, normStr, parts)
    for i, raw in enumerate(lines):
        line = raw.strip()
        m = _RE_STAMP.search(line)
        token = (m.group(1) or m.group(2)) if m else None
        if not token:
            m2 = _RE_DOTTED.search(line)
            if m2:
                token = m2.group(1)
        if token and '.' in token:
            norm = _norm(token)
            parts = norm.split('.')
            if all(p.isdigit() for p in parts) and 2 <= len(parts) <= 4:
                dotted_entries.append((i + 1, norm, parts))

    if len(dotted_entries) >= 3:
        return _build_dotted_tree(dotted_entries)

    # ── Strategy B: section headers + flat verse stamps ───────────────────────
    sections   = []   # (lineNo, sectionLabel, sectionTitle)
    flat_verses = []  # (lineNo, verseNumStr, parentSectionIdx)

    for i, raw in enumerate(lines):
        line = raw.strip()
        # Check section header
        sm = _RE_SECTION.match(line)
        if sm:
            snum  = _norm(sm.group(1))
            title = sm.group(2).strip()
            sections.append({'label': snum, 'title': title[:60],
                             'lineNumber': i + 1, 'children': []})
            continue
        # Check verse stamp
        vm = _RE_STAMP.search(line)
        if vm:
            vnum = _norm(vm.group(1) or vm.group(2) or '')
            if '.' not in vnum and vnum.isdigit():
                flat_verses.append((i + 1, vnum))

    if sections:
        # Assign verses to the section they fall under
        sec_lines = [s['lineNumber'] for s in sections]
        for lineno, vnum in flat_verses:
            # Find which section this verse belongs to (last section before this line)
            idx = len(sec_lines) - 1
            for si in range(len(sec_lines) - 1, -1, -1):
                if sec_lines[si] <= lineno:
                    idx = si
                    break
            sections[idx]['children'].append({'label': vnum, 'lineNumber': lineno})
        return {'style': 'sectioned', 'nodes': sections}

    # ── Strategy C: no hierarchy, just flat verse stamps ─────────────────────
    if flat_verses:
        nodes = [{'label': v, 'lineNumber': ln} for ln, v in flat_verses]
        return {'style': 'flat', 'nodes': nodes}

    return {'style': 'none', 'nodes': []}


def _build_dotted_tree(entries):
    """Build a grouped tree from dotted verse numbers."""
    # Collect unique groups at each level
    depth = max(len(e[2]) for e in entries)
    if depth == 1:
        # All single-component — flat
        return {'style': 'flat',
                'nodes': [{'label': e[1], 'lineNumber': e[0]} for e in entries]}

    # Group by first part, then second part
    from collections import OrderedDict
    root_map = OrderedDict()  # root_label → { first_line, sub_map }

    for lineno, norm, parts in entries:
        r = parts[0]
        if r not in root_map:
            root_map[r] = {'label': r, 'lineNumber': lineno, 'children': OrderedDict()}
        sub = root_map[r]['children']

        if len(parts) >= 2:
            r2 = f"{parts[0]}.{parts[1]}"
            if r2 not in sub:
                sub[r2] = {'label': r2, 'lineNumber': lineno, 'children': []}
            if len(parts) >= 3:
                sub[r2]['children'].append({'label': norm, 'lineNumber': lineno})

    # Flatten OrderedDicts to lists
    nodes = []
    for root in root_map.values():
        children = []
        for sub in root['children'].values():
            leaf = {'label': sub['label'], 'lineNumber': sub['lineNumber'],
                    'children': sub['children']}
            children.append(leaf)
        nodes.append({'label': root['label'], 'lineNumber': root['lineNumber'],
                      'children': children})

    return {'style': 'dotted', 'nodes': nodes}


# ── HTTP handler ──────────────────────────────────────────────────────────────

class Handler(BaseHTTPRequestHandler):

    def log_message(self, fmt, *args):
        print(f'[{self.address_string()}] {fmt % args}')

    def send_json(self, data, status=200):
        body = json.dumps(data, ensure_ascii=False).encode('utf-8')
        self.send_response(status)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def send_file(self, abs_path, mime):
        with open(abs_path, 'rb') as f:
            data = f.read()
        self.send_response(200)
        self.send_header('Content-Type', mime)
        self.send_header('Content-Length', str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def send_html_file(self, name):
        path = os.path.join(APP_DIR, name)
        if os.path.exists(path):
            self.send_file(path, 'text/html; charset=utf-8')
        else:
            self.send_response(404)
            self.end_headers()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path   = parsed.path
        qs     = urllib.parse.parse_qs(parsed.query)

        def qget(k, default=''):
            return qs.get(k, [default])[0]

        # ── API routes ────────────────────────────────────────────────────────
        if path == '/api/tree':
            self.send_json(build_tree(BASE_DIR, ''))
            return

        if path == '/api/file':
            rel = qget('path')
            if not rel:
                self.send_json({'error': 'path required'}, 400); return
            abs_f = safe_path(rel)
            if not abs_f:
                self.send_json({'error': 'Access denied'}, 403); return
            try:
                content = read_text_file(abs_f)
                all_lines = content.splitlines()
                total_lines = len(all_lines)
                limit_str = qget('limit', '')
                if limit_str:
                    offset = int(qget('offset', '0'))
                    limit  = int(limit_str)
                    chunk  = all_lines[offset:offset + limit]
                    self.send_json({'lines': chunk, 'totalLines': total_lines, 'offset': offset})
                else:
                    # Legacy: return full content (outline builder still uses this internally)
                    self.send_json({'content': content, 'totalLines': total_lines})
            except Exception as e:
                self.send_json({'error': str(e)}, 500)
            return

        if path == '/api/outline':
            rel = qget('path')
            if not rel:
                self.send_json({'error': 'path required'}, 400); return
            abs_f = safe_path(rel)
            if not abs_f:
                self.send_json({'error': 'Access denied'}, 403); return
            try:
                content = read_text_file(abs_f)
                self.send_json(build_outline(content))
            except Exception as e:
                self.send_json({'error': str(e)}, 500)
            return

        if path == '/api/search':
            q      = qget('q')
            script = qget('script', 'devanagari')
            paths  = [p.strip() for p in qget('paths').split(',') if p.strip()]
            fuzzy  = qget('fuzzy') == 'true'
            rx     = qget('regex') == 'true'
            offset = int(qget('offset', '0'))
            limit  = min(int(qget('limit', '20')), 100)
            if not q.strip():
                self.send_json({'results': [], 'total': 0}); return
            result = do_search(q, script, paths, fuzzy, rx, offset, limit)
            if 'error' in result:
                self.send_json(result, 400)
            else:
                self.send_json(result)
            return

        # ── Static files ──────────────────────────────────────────────────────
        if path in ('/', '/index.html'):
            self.send_html_file('index.html'); return

        if path == '/search' or path == '/search.html':
            self.send_html_file('search.html'); return

        # Serve files from APP_DIR/static/
        if path.startswith('/static/'):
            rel_static = path[1:]  # strip leading /
            abs_static = os.path.normpath(os.path.join(APP_DIR, rel_static))
            if not abs_static.startswith(APP_DIR):
                self.send_response(403); self.end_headers(); return
            if os.path.isfile(abs_static):
                ext  = os.path.splitext(abs_static)[1]
                mime = MIME.get(ext, 'application/octet-stream')
                self.send_file(abs_static, mime)
            else:
                self.send_response(404); self.end_headers()
            return

        self.send_response(404); self.end_headers()


# ── Entry point ───────────────────────────────────────────────────────────────
if __name__ == '__main__':
    server = HTTPServer(('0.0.0.0', PORT), Handler)
    print(f'Jaya Grantha Viewer running at http://localhost:{PORT}')
    print(f'Serving texts from: {BASE_DIR}')
    print('Press Ctrl+C to stop.')
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print('\nStopped.')
