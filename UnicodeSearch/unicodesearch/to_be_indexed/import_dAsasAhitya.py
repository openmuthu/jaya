#!/usr/bin/env python3
"""
Unified dAsasAhitya importer.

Reads source compositions from SOURCE_DIR, classifies each as:
  - ugAbhOga  (first non-empty line is exactly 'ಉಗಾಭೋಗ')
  - suLAdi    (any of the first 3 non-empty lines contains 'ಸುಳಾದಿ')
  - kIrtane   (everything else with recognisable pallavi / dhruva / first-line)

Output structure:
  OUTPUT_DIR/<composer>/<type>/<id>-<itrans-title>.txt

where <id> is the composition number from the ---N--- delimiter in comps.txt.

Content is written in Devanagari (converted inline from Kannada).
First line of each file is the clean title (no markers), followed by all
composition lines from the source.
"""

import os
import re

SOURCE_DIR = "/Users/vjutur/Documents/Investigations/Claude Tests/dAsasAhitya/data/saints/"
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "dAsasAhitya")

# ── Kannada → Devanagari (block offset 0x0380) ────────────────────────────────

KANNADA_START = 0x0C80
KANNADA_END   = 0x0CFF
OFFSET        = 0x0380  # Devanagari block starts 0x0380 below Kannada block


def kannada_to_devanagari(text):
    return ''.join(
        chr(ord(ch) - OFFSET) if KANNADA_START <= ord(ch) <= KANNADA_END else ch
        for ch in text
    )


# ── Devanagari → ITRANS ───────────────────────────────────────────────────────

CONSONANTS = {
    'क': 'k',  'ख': 'kh', 'ग': 'g',  'घ': 'gh', 'ङ': 'G',
    'च': 'c',  'छ': 'Ch', 'ज': 'j',  'झ': 'jh', 'ञ': 'JN',
    'ट': 'T',  'ठ': 'Th', 'ड': 'D',  'ढ': 'Dh', 'ण': 'N',
    'त': 't',  'थ': 'th', 'द': 'd',  'ध': 'dh', 'न': 'n',
    'प': 'p',  'फ': 'ph', 'ब': 'b',  'भ': 'bh', 'म': 'm',
    'य': 'y',  'र': 'r',  'ल': 'l',  'व': 'v',
    'श': 'sh', 'ष': 'Sh', 'स': 's',  'ह': 'h',  'ळ': 'L',
    'ऱ': 'r',
}
MATRAS = {
    'ा': 'A', 'ि': 'i', 'ी': 'I', 'ु': 'u', 'ू': 'U',
    'ृ': 'Ru',
    'ॆ': 'e', 'े': 'E', 'ै': 'ai',
    'ॊ': 'o', 'ो': 'O', 'ौ': 'au',
    '्': '',
}
INDEPENDENT_VOWELS = {
    'अ': 'a', 'आ': 'A', 'इ': 'i', 'ई': 'I', 'उ': 'u', 'ऊ': 'U',
    'ऋ': 'Ru',
    'ऎ': 'e', 'ए': 'E', 'ऐ': 'ai',
    'ऒ': 'o', 'ओ': 'O', 'औ': 'au',
}
SPECIAL = {'ं': 'M', 'ः': 'H', 'ँ': 'M'}


def devanagari_to_itrans(text):
    result = []
    chars = list(text)
    n = len(chars)
    i = 0
    while i < n:
        ch = chars[i]
        if ch in CONSONANTS:
            if i + 1 < n and chars[i + 1] in MATRAS:
                result.append(CONSONANTS[ch] + MATRAS[chars[i + 1]])
                i += 2
            else:
                result.append(CONSONANTS[ch] + 'a')
                i += 1
        elif ch in INDEPENDENT_VOWELS:
            result.append(INDEPENDENT_VOWELS[ch])
            i += 1
        elif ch in MATRAS:
            result.append(MATRAS[ch])
            i += 1
        elif ch in SPECIAL:
            result.append(SPECIAL[ch])
            i += 1
        elif ch == ' ':
            result.append(' ')
            i += 1
        elif ch.isascii() and ch.isalnum():
            result.append(ch)
            i += 1
        else:
            i += 1
    return ''.join(result)


def make_filename(comp_id, devanagari_title, taken):
    """
    Build '<id>-<itrans-slug>.txt', deduplicating within `taken`.
    Returns None if the slug is empty.
    """
    itrans = devanagari_to_itrans(devanagari_title)
    slug = itrans[:50].rstrip(' -').replace(' ', '-')
    slug = re.sub(r'[^a-zA-Z0-9\-_]', '', slug).rstrip('-')

    prefix = str(comp_id)
    if slug:
        base = f'{prefix}-{slug}'
    else:
        base = prefix  # fallback: id only

    candidate = base + '.txt'
    if candidate not in taken:
        return candidate
    counter = 2
    while True:
        candidate = f'{base}-{counter}.txt'
        if candidate not in taken:
            return candidate
        counter += 1


# ── Classification helpers ────────────────────────────────────────────────────

# Tala / section keywords that are NOT the composition title themselves
TALA_RE = re.compile(
    r'^(ಧ್ರುವತಾಳ|ಮಟ್ಟತಾಳ|ಮಠ್ಯತಾಳ|ತ್ರಿವಿಡಿತಾಳ|ಜಂಪೆತಾಳ|ಝಂಪೆತಾಳ|ಅಟ್ಟತಾಳ|ರೂಪಕತಾಳ|'
    r'ಏಕತಾಳ|ಆದಿತಾಳ|ತ್ರಿಪುಟತಾಳ|ಅಷ್ಟತಾಳ|ಸೂಳಾದಿ|ಜತೆ|ತ್ರಿಪದಿ|ಷಟ್ಪದಿ)\s*$'
)

# Any line that is purely a type or tala keyword (possibly combined, e.g. 'ಸುಳಾದಿ ಧ್ರುವತಾಳ')
HEADER_KEYWORDS = re.compile(
    r'^[\s]*(ಉಗಾಭೋಗ|ಸುಳಾದಿ|ಧ್ರುವತಾಳ|ಮಟ್ಟತಾಳ|ಮಠ್ಯತಾಳ|ತ್ರಿವಿಡಿತಾಳ|ಜಂಪೆತಾಳ|'
    r'ಝಂಪೆತಾಳ|ಅಟ್ಟತಾಳ|ರೂಪಕತಾಳ|ಏಕತಾಳ|ಆದಿತಾಳ|ತ್ರಿಪುಟತಾಳ|ಅಷ್ಟತಾಳ|'
    r'ಸೂಳಾದಿ|ಜತೆ|ತ್ರಿಪದಿ|ಷಟ್ಪದಿ)[\s]*(ಧ್ರುವತಾಳ|ಮಟ್ಟತಾಳ|ಮಠ್ಯತಾಳ|ತ್ರಿವಿಡಿತಾಳ|'
    r'ಜಂಪೆತಾಳ|ಝಂಪೆತಾಳ|ಅಟ್ಟತಾಳ|ರೂಪಕತಾಳ|ಏಕತಾಳ|ಆದಿತಾಳ|ತ್ರಿಪುಟತಾಳ)?[\s]*$'
)

PALLAVI_END_RE = re.compile(
    r'\s+(ಪ|ಪ\.|ಅ\.ಪ|ಅ\.ಪ\.|ಧ್ರುವ|ಧ್ರು|ಧ್ರುವ\|)\s*$'
)

SECTION_HEADER_RE = re.compile(
    r'^(ಉಗಾಭೋಗ|ಧ್ರುವತಾಳ|ಮಟ್ಟತಾಳ|ಮಠ್ಯತಾಳ|ತ್ರಿವಿಡಿತಾಳ|ಜಂಪೆತಾಳ|ಝಂಪೆತಾಳ|'
    r'ಅಟ್ಟತಾಳ|ರೂಪಕತಾಳ|ಏಕತಾಳ|ಆದಿತಾಳ|ತ್ರಿಪುಟತಾಳ)\s*$'
)

# Minimum ITRANS slug length to be considered a real content title
MIN_TITLE_SLUG_LEN = 10


def is_header_line(line):
    """Return True if the line is purely a type/tala keyword (not real content)."""
    return bool(HEADER_KEYWORDS.match(line.strip()))


def find_content_title(non_empty_lines):
    """
    Find the first line in non_empty_lines that is actual composition content
    (not a type/tala keyword header).  Returns None if nothing suitable is found.
    """
    for line in non_empty_lines:
        s = line.strip()
        if not s:
            continue
        if is_header_line(s):
            continue
        return line
    return None


def classify(non_empty_lines):
    """
    Return ('ugAbhOga'|'suLAdi'|'kIrtane', title_line_kn) or (None, None).
    title_line_kn is the raw Kannada title line (markers still attached).
    For ugAbhOga and suLAdi the title is always the first real content line
    (not the type/tala keyword header).
    """
    if len(non_empty_lines) < 3:
        return None, None

    first = non_empty_lines[0].strip()

    # ugAbhOga: first line is exactly 'ಉಗಾಭೋಗ'
    if first == 'ಉಗಾಭೋಗ':
        title = find_content_title(non_empty_lines[1:])
        return 'ugAbhOga', title if title else non_empty_lines[1]

    # suLAdi: any of first 3 lines contains 'ಸುಳಾದಿ'
    for idx, line in enumerate(non_empty_lines[:3]):
        if 'ಸುಳಾದಿ' in line:
            # Skip all keyword headers and find first real content line
            title = find_content_title(non_empty_lines[idx + 1:])
            return 'suLAdi', title if title else line

    # kIrtane: look for pallavi / dhruva marker in first 3 lines
    for line in non_empty_lines[:3]:
        if PALLAVI_END_RE.search(line.rstrip()):
            return 'kIrtane', line

    # kIrtane with section-header first line → title is second line
    if SECTION_HEADER_RE.match(first):
        if len(non_empty_lines) >= 2:
            return 'kIrtane', non_empty_lines[1]
        return None, None

    # kIrtane: no marker → first line is title
    return 'kIrtane', non_empty_lines[0]


def strip_title_markers(line):
    """Remove trailing pallavi/dhruva markers and punctuation from a title line."""
    line = PALLAVI_END_RE.sub('', line.rstrip()).strip()
    line = re.sub(r'[\s|॥।]+$', '', line).strip()
    return line


# ── Saint folder mapping ──────────────────────────────────────────────────────

SAINT_FOLDER_MAP = {
    'ಗೋಪಾಲದಾಸರು': 'gOpAladAsaru',
    'ಕನಕದಾಸರು': 'kanakadAsaru',
    'ಗುರು ಜಗನ್ನಾಥದಾಸರು': 'jagannAthadAsaru',
    'ಜಗನ್ನಾಥದಾಸರು': 'jagannAthadAsaru',
    'ವಾದಿರಾಜ': 'vAdirAjaru',
    'ವಿಜಯದಾಸ': 'vijayadAsaru',
}

KN_CONSONANTS = {
    'ಕ': 'k', 'ಖ': 'kh', 'ಗ': 'g', 'ಘ': 'gh', 'ಙ': 'G',
    'ಚ': 'c', 'ಛ': 'Ch', 'ಜ': 'j', 'ಝ': 'jh', 'ಞ': 'JN',
    'ಟ': 'T', 'ಠ': 'Th', 'ಡ': 'D', 'ಢ': 'Dh', 'ಣ': 'N',
    'ತ': 't', 'ಥ': 'th', 'ದ': 'd', 'ಧ': 'dh', 'ನ': 'n',
    'ಪ': 'p', 'ಫ': 'ph', 'ಬ': 'b', 'ಭ': 'bh', 'ಮ': 'm',
    'ಯ': 'y', 'ರ': 'r', 'ಲ': 'l', 'ವ': 'v',
    'ಶ': 'sh', 'ಷ': 'Sh', 'ಸ': 's', 'ಹ': 'h', 'ಳ': 'L',
}
KN_VOWELS = {
    'ಅ': 'a', 'ಆ': 'A', 'ಇ': 'i', 'ಈ': 'I', 'ಉ': 'u', 'ಊ': 'U',
    'ಋ': 'Ru', 'ಎ': 'e', 'ಏ': 'E', 'ಐ': 'ai', 'ಒ': 'o', 'ಓ': 'O', 'ಔ': 'au',
}
KN_MATRAS = {
    'ಾ': 'A', 'ಿ': 'i', 'ೀ': 'I', 'ು': 'u', 'ೂ': 'U',
    'ೃ': 'Ru', 'ೆ': 'e', 'ೇ': 'E', 'ೈ': 'ai', 'ೊ': 'o', 'ೋ': 'O', 'ೌ': 'au',
    '್': '',
}
KN_SPECIAL = {'ಂ': 'M', 'ಃ': 'H'}


def kannada_to_itrans(text):
    result = []
    chars = list(text)
    i = 0
    while i < len(chars):
        ch = chars[i]
        if ch in KN_CONSONANTS:
            if i + 1 < len(chars) and chars[i + 1] in KN_MATRAS:
                result.append(KN_CONSONANTS[ch] + KN_MATRAS[chars[i + 1]])
                i += 2
            else:
                result.append(KN_CONSONANTS[ch] + 'a')
                i += 1
        elif ch in KN_VOWELS:
            result.append(KN_VOWELS[ch])
            i += 1
        elif ch in KN_MATRAS:
            result.append(KN_MATRAS[ch])
            i += 1
        elif ch in KN_SPECIAL:
            result.append(KN_SPECIAL[ch])
            i += 1
        elif ch == ' ':
            result.append(' ')
            i += 1
        elif ch.isascii():
            result.append(ch)
            i += 1
        else:
            result.append(ch)
            i += 1
    return ''.join(result)


def saint_to_folder_name(saint_name):
    if saint_name in SAINT_FOLDER_MAP:
        return SAINT_FOLDER_MAP[saint_name]
    itrans = kannada_to_itrans(saint_name)
    return itrans.replace(' ', '-')


# ── Parsing ───────────────────────────────────────────────────────────────────

def parse_compositions(comps_file):
    """Return list of (number_str, lines) from a comps.txt file."""
    compositions = []
    current_number = None
    current_lines = []
    with open(comps_file, encoding='utf-8') as f:
        for raw_line in f:
            line = raw_line.rstrip('\n').rstrip('\r')
            m = re.match(r'^---(\d+)---\s*$', line)
            if m:
                if current_number is not None:
                    compositions.append((current_number, current_lines))
                current_number = m.group(1)
                current_lines = []
            elif current_number is not None:
                current_lines.append(line)
    if current_number is not None and current_lines:
        compositions.append((current_number, current_lines))
    return compositions


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    counts = {'ugAbhOga': 0, 'suLAdi': 0, 'kIrtane': 0, 'skipped': 0}

    for saint_name in sorted(os.listdir(SOURCE_DIR)):
        saint_path = os.path.join(SOURCE_DIR, saint_name)
        if not os.path.isdir(saint_path):
            continue
        comps_file = os.path.join(saint_path, 'comps.txt')
        if not os.path.exists(comps_file):
            continue

        folder_name = saint_to_folder_name(saint_name)
        saint_base = os.path.join(OUTPUT_DIR, folder_name)

        # Create type subfolders
        for subfolder in ('ugAbhOga', 'suLAdi', 'kIrtane'):
            os.makedirs(os.path.join(saint_base, subfolder), exist_ok=True)

        # Per-type taken sets (to deduplicate filenames within each subfolder)
        taken = {'ugAbhOga': set(), 'suLAdi': set(), 'kIrtane': set()}

        compositions = parse_compositions(comps_file)
        saint_counts = {'ugAbhOga': 0, 'suLAdi': 0, 'kIrtane': 0, 'skipped': 0}

        for comp_id, lines in compositions:
            non_empty = [l for l in lines if l.strip()]

            comp_type, title_line_kn = classify(non_empty)

            if comp_type is None or title_line_kn is None:
                saint_counts['skipped'] += 1
                counts['skipped'] += 1
                continue

            # Clean title: strip markers → Devanagari → ITRANS slug
            title_kn = strip_title_markers(title_line_kn)
            title_dev = kannada_to_devanagari(title_kn)

            filename = make_filename(comp_id, title_dev, taken[comp_type])
            taken[comp_type].add(filename)

            filepath = os.path.join(saint_base, comp_type, filename)

            # File content: clean title (Devanagari) + all composition lines (Devanagari)
            dev_lines = [title_dev] + [kannada_to_devanagari(l) for l in lines]
            content = '\r\n'.join(dev_lines) + '\r\n'

            with open(filepath, 'w', encoding='utf-8', newline='') as f:
                f.write(content)

            saint_counts[comp_type] += 1
            counts[comp_type] += 1

        total_saint = sum(saint_counts[t] for t in ('ugAbhOga', 'suLAdi', 'kIrtane'))
        if total_saint or saint_counts['skipped']:
            print(f'  {saint_name} -> {folder_name}: '
                  f'{saint_counts["kIrtane"]} kIrtane, '
                  f'{saint_counts["ugAbhOga"]} ugAbhOga, '
                  f'{saint_counts["suLAdi"]} suLAdi, '
                  f'{saint_counts["skipped"]} skipped')

    print()
    print('=' * 60)
    print(f'kIrtane:   {counts["kIrtane"]}')
    print(f'ugAbhOga:  {counts["ugAbhOga"]}')
    print(f'suLAdi:    {counts["suLAdi"]}')
    print(f'skipped:   {counts["skipped"]}')
    print(f'TOTAL:     {sum(counts[t] for t in ("kIrtane", "ugAbhOga", "suLAdi"))}')


if __name__ == '__main__':
    main()
