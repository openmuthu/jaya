#!/usr/bin/env python3
"""
Import dAsasAhitya compositions that were skipped by the original converter.

The original convert_dAsasAhitya.py only recognised lines ending in ' ಪ' or ' ಪ.'
as the pallavi/title marker.  Many compositions use other markers:
  - ಧ್ರುವ / ಧ್ರು    (dhruva — refrain, semantically same as pallavi)
  - ಧ್ರುವತಾಳ        (dhruva-tala — first line names the section, title is line 2)
  - ಉಗಾಭೋಗ          (ugabhoga — first line names the type, title is line 2)
  - ಸುಳಾದಿ           (suLadi — first line includes type, itself is the title)
  - No marker         (plain verses — first line is title)

This script only ADDS new files (skips if file already exists in the output folder).
Content is written in Devanagari (converted inline) with correct ITRANS filenames.
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


def make_filename(devanagari_title, preexisting, new_taken):
    """
    Convert Devanagari title to a safe ITRANS filename.

    - If the canonical (unsuffixed) name is already in `preexisting`, return None
      (the composition was already imported in a previous run — skip it).
    - Deduplicate only among the new files being added in this run (using `new_taken`).
    """
    itrans = devanagari_to_itrans(devanagari_title)
    slug = itrans[:50].rstrip(' -').replace(' ', '-')
    slug = re.sub(r'[^a-zA-Z0-9\-_]', '', slug).rstrip('-')
    if not slug:
        return None
    canonical = slug + '.txt'
    # If canonical name is taken by a pre-existing file, skip — already imported
    if canonical in preexisting:
        return None
    if canonical not in new_taken:
        return canonical
    counter = 2
    while True:
        candidate = f'{slug}-{counter}.txt'
        if candidate not in preexisting and candidate not in new_taken:
            return candidate
        counter += 1


# ── Pallavi / title detection ─────────────────────────────────────────────────

# Markers that appear at the END of the title/pallavi line
PALLAVI_END_RE = re.compile(
    r'\s+(ಪ|ಪ\.|ಅ\.ಪ|ಅ\.ಪ\.|ಧ್ರುವ|ಧ್ರು|ಧ್ರುವ\|)\s*$'
)

# Markers that appear as the FIRST LINE, meaning the title is the NEXT line
SECTION_HEADER_RE = re.compile(
    r'^(ಉಗಾಭೋಗ|ಧ್ರುವತಾಳ|ಮಟ್ಟತಾಳ|ತ್ರಿವಿಡಿತಾಳ|ಜಂಪೆತಾಳ|ಅಟ್ಟತಾಳ|ರೂಪಕತಾಳ|ಏಕತಾಳ|ಆದಿತಾಳ)\s*$'
)


def get_title_line(lines):
    """
    Return the title line (in Kannada) for a composition.
    Returns None if the composition should be skipped.
    """
    non_empty = [l for l in lines if l.strip()]
    if len(non_empty) < 3:
        return None

    # Case 1: first line is a section-header keyword → title is next line
    if SECTION_HEADER_RE.match(non_empty[0].strip()):
        if len(non_empty) >= 2:
            return non_empty[1]
        return None

    # Case 2: one of the first 3 lines ends with a pallavi/dhruva marker
    for line in non_empty[:3]:
        if PALLAVI_END_RE.search(line.rstrip()):
            return line

    # Case 3: suladi — first line contains 'ಸುಳಾದಿ' (title includes the type name)
    if 'ಸುಳಾದಿ' in non_empty[0]:
        return non_empty[0]

    # Case 4: no marker → use the first non-empty line as title
    return non_empty[0]


def strip_title_markers(line):
    """Remove trailing pallavi/section markers from a title line."""
    line = PALLAVI_END_RE.sub('', line.rstrip()).strip()
    # Also strip trailing danḍas, Kannada digits and punctuation
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

# ── Kannada → ITRANS (for saint folder names, not reused for content) ─────────

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
    total_written = 0
    total_skipped = 0
    total_exists = 0

    for saint_name in sorted(os.listdir(SOURCE_DIR)):
        saint_path = os.path.join(SOURCE_DIR, saint_name)
        if not os.path.isdir(saint_path):
            continue
        comps_file = os.path.join(saint_path, 'comps.txt')
        if not os.path.exists(comps_file):
            continue

        folder_name = saint_to_folder_name(saint_name)
        output_folder = os.path.join(OUTPUT_DIR, folder_name)
        os.makedirs(output_folder, exist_ok=True)

        preexisting = set(os.listdir(output_folder))
        new_taken = set()
        compositions = parse_compositions(comps_file)

        saint_written = saint_skipped = saint_exists = 0

        for comp_number, lines in compositions:
            title_line_kn = get_title_line(lines)
            if title_line_kn is None:
                total_skipped += 1
                saint_skipped += 1
                continue

            # Clean title: strip markers, convert to Devanagari
            title_kn = strip_title_markers(title_line_kn)
            title_dev = kannada_to_devanagari(title_kn)

            # Generate filename — returns None if canonical name is already in preexisting
            filename = make_filename(title_dev, preexisting, new_taken)
            if filename is None:
                total_exists += 1
                saint_exists += 1
                continue

            filepath = os.path.join(output_folder, filename)

            # Build content: title line (Devanagari) + all composition lines (Devanagari)
            dev_lines = [title_dev] + [kannada_to_devanagari(l) for l in lines]
            content = '\r\n'.join(dev_lines) + '\r\n'

            with open(filepath, 'w', encoding='utf-8', newline='') as f:
                f.write(content)

            new_taken.add(filename)
            total_written += 1
            saint_written += 1

        if saint_written:
            print(f'  {saint_name} -> {folder_name}: +{saint_written} new'
                  f' (skipped {saint_skipped}, already existed {saint_exists})')

    print()
    print('=' * 60)
    print(f'New compositions written: {total_written}')
    print(f'Already existed (skipped): {total_exists}')
    print(f'Skipped (too short / no content): {total_skipped}')


if __name__ == '__main__':
    main()
