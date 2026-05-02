#!/usr/bin/env python3
"""
Fix dAsasAhitya filenames: rename files with all-lowercase names to
proper case-sensitive ITRANS filenames derived from their Devanagari content.

The original conversion script called .lower() on the ITRANS transliteration,
destroying case semantics (A=long-a, I=long-i, Sh=retroflex-sibilant, etc.).
This script fixes that by re-deriving filenames from the Devanagari first line.
"""

import os
import re

DASA_DIR = os.path.join(os.path.dirname(__file__), "dAsasAhitya")

# ── Devanagari → ITRANS tables ────────────────────────────────────────────────

CONSONANTS = {
    'क': 'k',  'ख': 'kh', 'ग': 'g',  'घ': 'gh', 'ङ': 'G',
    'च': 'c',  'छ': 'Ch', 'ज': 'j',  'झ': 'jh', 'ञ': 'JN',
    'ट': 'T',  'ठ': 'Th', 'ड': 'D',  'ढ': 'Dh', 'ण': 'N',
    'त': 't',  'थ': 'th', 'द': 'd',  'ध': 'dh', 'न': 'n',
    'प': 'p',  'फ': 'ph', 'ब': 'b',  'भ': 'bh', 'म': 'm',
    'य': 'y',  'र': 'r',  'ल': 'l',  'व': 'v',
    'श': 'sh', 'ष': 'Sh', 'स': 's',  'ह': 'h',  'ळ': 'L',
    'ऱ': 'r',  # rare Devanagari RA
}

# Dependent vowel signs (matras) + virama
MATRAS = {
    'ा': 'A',  'ि': 'i',  'ी': 'I',  'ु': 'u',  'ू': 'U',
    'ृ': 'Ru',
    'ॆ': 'e',  'े': 'E',  'ै': 'ai',
    'ॊ': 'o',  'ो': 'O',  'ौ': 'au',
    '्': '',   # virama — suppresses inherent 'a'
}

# Independent vowels
INDEPENDENT_VOWELS = {
    'अ': 'a', 'आ': 'A',
    'इ': 'i', 'ई': 'I',
    'उ': 'u', 'ऊ': 'U',
    'ऋ': 'Ru',
    'ऎ': 'e', 'ए': 'E',  'ऐ': 'ai',
    'ऒ': 'o', 'ओ': 'O',  'औ': 'au',
}

SPECIAL = {
    'ं': 'M',  # anusvara
    'ः': 'H',  # visarga
    'ँ': 'M',  # chandrabindu
}


def devanagari_to_itrans(text):
    """Convert a Devanagari string to ITRANS (case-sensitive)."""
    result = []
    chars = list(text)
    n = len(chars)
    i = 0
    while i < n:
        ch = chars[i]
        if ch in CONSONANTS:
            # Check next character for matra or virama
            if i + 1 < n and chars[i + 1] in MATRAS:
                matra_str = MATRAS[chars[i + 1]]
                result.append(CONSONANTS[ch] + matra_str)
                i += 2
            else:
                # No matra follows — inherent 'a'
                result.append(CONSONANTS[ch] + 'a')
                i += 1
        elif ch in INDEPENDENT_VOWELS:
            result.append(INDEPENDENT_VOWELS[ch])
            i += 1
        elif ch in MATRAS:
            # Orphan matra (unusual) — emit as-is
            result.append(MATRAS[ch])
            i += 1
        elif ch in SPECIAL:
            result.append(SPECIAL[ch])
            i += 1
        elif ch == ' ':
            result.append(' ')
            i += 1
        elif ch.isascii():
            # Keep ASCII digits/letters, drop punctuation and control chars
            if ch.isalnum():
                result.append(ch)
            i += 1
        else:
            # Unknown non-ASCII (shouldn't happen after conversion) — skip
            i += 1
    return ''.join(result)


def make_filename(itrans, taken):
    """
    Turn an ITRANS string into a safe filename (case-preserved), avoiding `taken`.
    Returns '<slug>.txt' or None if slug is empty.
    """
    # Limit to 50 characters, replace spaces with hyphens, strip punctuation
    slug = itrans[:50].rstrip(' -')
    slug = slug.replace(' ', '-')
    slug = re.sub(r'[^a-zA-Z0-9\-_]', '', slug)
    slug = slug.rstrip('-')

    if not slug:
        return None

    candidate = slug + '.txt'
    if candidate not in taken:
        return candidate

    counter = 2
    while True:
        candidate = f"{slug}-{counter}.txt"
        if candidate not in taken:
            return candidate
        counter += 1


def is_newly_created(filename):
    """
    Return True if the filename is all-lowercase (was created with the buggy .lower() call).
    Pre-existing files have uppercase letters in their names.
    """
    base = filename[:-4]  # strip .txt
    return base == base.lower()


def fix_folder(folder_path):
    """Rename all newly-created (lowercase-only) files in folder_path."""
    all_files = [f for f in os.listdir(folder_path) if f.endswith('.txt')]
    new_files = sorted(f for f in all_files if is_newly_created(f))
    pre_existing = [f for f in all_files if not is_newly_created(f)]

    if not new_files:
        return 0, 0

    # Names already taken by pre-existing files (must not be overwritten)
    taken = set(pre_existing)

    # First pass: determine new names
    renames = {}   # old_name -> new_name
    no_change = 0

    for fname in new_files:
        fpath = os.path.join(folder_path, fname)
        try:
            with open(fpath, encoding='utf-8') as f:
                first_line = f.readline().strip()
        except UnicodeDecodeError:
            # Fall back to UTF-16 (pre-existing files that were skipped earlier)
            try:
                with open(fpath, encoding='utf-16') as f:
                    first_line = f.readline().strip()
            except Exception:
                first_line = ''

        itrans = devanagari_to_itrans(first_line)
        new_name = make_filename(itrans, taken | set(renames.values()))

        if new_name is None or new_name == fname:
            no_change += 1
            taken.add(fname)  # keep in taken so others don't collide
            continue

        renames[fname] = new_name
        taken.add(new_name)

    if not renames:
        return 0, no_change

    # Second pass: rename via temp files to avoid collisions
    temp_map = {}
    for old_name in renames:
        tmp = old_name + '.__tmp__'
        os.rename(os.path.join(folder_path, old_name), os.path.join(folder_path, tmp))
        temp_map[tmp] = renames[old_name]

    for tmp, new_name in temp_map.items():
        os.rename(os.path.join(folder_path, tmp), os.path.join(folder_path, new_name))

    return len(renames), no_change


def main():
    total_renamed = 0
    total_unchanged = 0

    for saint_folder in sorted(os.listdir(DASA_DIR)):
        folder_path = os.path.join(DASA_DIR, saint_folder)
        if not os.path.isdir(folder_path):
            continue

        renamed, unchanged = fix_folder(folder_path)
        if renamed or unchanged:
            print(f"  {saint_folder}: {renamed} renamed, {unchanged} unchanged")
        total_renamed += renamed
        total_unchanged += unchanged

    print()
    print('=' * 60)
    print(f"Total renamed:    {total_renamed}")
    print(f"Total unchanged:  {total_unchanged}")


if __name__ == '__main__':
    main()
