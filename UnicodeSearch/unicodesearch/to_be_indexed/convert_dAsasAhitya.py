#!/usr/bin/env python3
"""
Convert dAsasAhitya compositions into the format needed by the Jaya Android app.

Source: /Users/vjutur/Documents/Investigations/Claude Tests/dAsasAhitya/data/saints/
Output: /Users/vjutur/Documents/github/jaya/UnicodeSearch/unicodesearch/to_be_indexed/dAsasAhitya/
"""

import os
import re
import sys

SOURCE_DIR = "/Users/vjutur/Documents/Investigations/Claude Tests/dAsasAhitya/data/saints/"
OUTPUT_DIR = "/Users/vjutur/Documents/github/jaya/UnicodeSearch/unicodesearch/to_be_indexed/dAsasAhitya/"

# Explicit folder name overrides for known saints
SAINT_FOLDER_MAP = {
    'ಗೋಪಾಲದಾಸರು': 'gOpAladAsaru',
    'ಕನಕದಾಸರು': 'kanakadAsaru',
    'ಗುರು ಜಗನ್ನಾಥದಾಸರು': 'jagannAthadAsaru',
    'ಜಗನ್ನಾಥದಾಸರು': 'jagannAthadAsaru',
    'ವಾದಿರಾಜ': 'vAdirAjaru',
    'ವಿಜಯದಾಸ': 'vijayadAsaru',
}


def kannada_to_itrans(text):
    """Convert Kannada Unicode text to ITRANS transliteration."""
    # Independent vowels
    vowels = {
        'ಅ': 'a', 'ಆ': 'A', 'ಇ': 'i', 'ಈ': 'I', 'ಉ': 'u', 'ಊ': 'U',
        'ಋ': 'Ru', 'ಎ': 'e', 'ಏ': 'E', 'ಐ': 'ai', 'ಒ': 'o', 'ಓ': 'O', 'ಔ': 'au',
    }
    # Dependent vowel signs (matras) and halanta
    matras = {
        'ಾ': 'A', 'ಿ': 'i', 'ೀ': 'I', 'ು': 'u', 'ೂ': 'U',
        'ೃ': 'Ru', 'ೆ': 'e', 'ೇ': 'E', 'ೈ': 'ai', 'ೊ': 'o', 'ೋ': 'O', 'ೌ': 'au',
        '್': '',  # halanta/virama — suppresses inherent vowel
    }
    # Consonants (inherent 'a' added unless followed by matra or halanta)
    consonants = {
        'ಕ': 'k', 'ಖ': 'kh', 'ಗ': 'g', 'ಘ': 'gh', 'ಙ': 'G',
        'ಚ': 'c', 'ಛ': 'Ch', 'ಜ': 'j', 'ಝ': 'jh', 'ಞ': 'JN',
        'ಟ': 'T', 'ಠ': 'Th', 'ಡ': 'D', 'ಢ': 'Dh', 'ಣ': 'N',
        'ತ': 't', 'ಥ': 'th', 'ದ': 'd', 'ಧ': 'dh', 'ನ': 'n',
        'ಪ': 'p', 'ಫ': 'ph', 'ಬ': 'b', 'ಭ': 'bh', 'ಮ': 'm',
        'ಯ': 'y', 'ರ': 'r', 'ಲ': 'l', 'ವ': 'v',
        'ಶ': 'sh', 'ಷ': 'Sh', 'ಸ': 's', 'ಹ': 'h', 'ಳ': 'L',
    }
    special = {'ಂ': 'M', 'ಃ': 'H'}

    result = []
    chars = list(text)
    i = 0
    while i < len(chars):
        ch = chars[i]
        if ch in consonants:
            # Look ahead for matra/halanta
            if i + 1 < len(chars) and chars[i + 1] in matras:
                m = chars[i + 1]
                result.append(consonants[ch] + matras[m])
                i += 2
            else:
                # Add inherent 'a'
                result.append(consonants[ch] + 'a')
                i += 1
        elif ch in vowels:
            result.append(vowels[ch])
            i += 1
        elif ch in matras:
            # Standalone matra (edge case)
            result.append(matras[ch])
            i += 1
        elif ch in special:
            result.append(special[ch])
            i += 1
        elif ch == ' ':
            result.append(' ')
            i += 1
        elif ch.isascii():
            result.append(ch)
            i += 1
        else:
            # Unknown Kannada character — pass through
            result.append(ch)
            i += 1

    return ''.join(result)


def saint_to_folder_name(saint_name):
    """Get ITRANS folder name for a saint, using explicit map or generating one."""
    if saint_name in SAINT_FOLDER_MAP:
        return SAINT_FOLDER_MAP[saint_name]
    # Generate from transliteration; spaces become hyphens in folder name
    itrans = kannada_to_itrans(saint_name)
    # Replace spaces with hyphens for folder name
    folder = itrans.replace(' ', '-')
    return folder


def pallavi_to_filename(pallavi_text, source_number, existing_filenames):
    """
    Convert pallavi text to a filename.
    - Transliterate to ITRANS
    - Take first 50 chars, trim trailing spaces/hyphens
    - Replace spaces with hyphens, lowercase
    - Deduplicate with -N suffix
    """
    # Strip trailing pallavi marker(s): ' ಪ.' or ' ಪ' etc.
    title = re.sub(r'\s+ಪ[\s.]*$', '', pallavi_text).strip()

    # Transliterate
    itrans = kannada_to_itrans(title)

    # Take first 50 chars and trim
    itrans = itrans[:50].rstrip(' -')

    # Replace spaces with hyphens, lowercase
    base = itrans.replace(' ', '-').lower()

    # Remove characters not safe for filenames (keep alphanumeric, hyphens, underscores)
    base = re.sub(r'[^a-z0-9\-_]', '', base)

    # Trim trailing hyphens again after cleanup
    base = base.rstrip('-')

    # Check for duplicates
    candidate = base + '.txt'
    if candidate not in existing_filenames:
        return candidate

    # Try with source number suffix
    candidate = f"{base}-{source_number}.txt"
    if candidate not in existing_filenames:
        return candidate

    # Fallback: increment counter
    counter = 2
    while True:
        candidate = f"{base}-{source_number}-{counter}.txt"
        if candidate not in existing_filenames:
            return candidate
        counter += 1


def strip_pallavi_marker(line):
    """Strip trailing ' ಪ.' or ' ಪ' markers from a line to get the title."""
    return re.sub(r'\s+ಪ[\s.]*$', '', line).strip()


def is_pallavi_line(line):
    """Return True if the line ends with a pallavi marker."""
    return bool(re.search(r'\s+ಪ[\s.]*$', line.rstrip()))


def parse_compositions(comps_file):
    """
    Parse a comps.txt file and return list of (number, lines) tuples.
    Each composition is everything from ---N--- until the next ---N--- or EOF.
    """
    compositions = []
    current_number = None
    current_lines = []

    with open(comps_file, 'r', encoding='utf-8') as f:
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


def should_skip(lines):
    """
    Return (True, reason) if composition should be skipped.
    Skip if:
    - fewer than 3 non-empty lines (excluding header)
    - no pallavi line found
    """
    non_empty = [l for l in lines if l.strip()]
    if len(non_empty) < 3:
        return True, f"only {len(non_empty)} non-empty lines"

    # Check for pallavi line — first non-empty line should have ` ಪ.` or ` ಪ`
    # But also allow pallavi anywhere in first few lines
    has_pallavi = False
    for line in non_empty[:3]:
        if is_pallavi_line(line):
            has_pallavi = True
            break

    if not has_pallavi:
        return True, "no pallavi line found"

    return False, None


def get_pallavi(lines):
    """Get the pallavi line (first non-empty line that is a pallavi)."""
    non_empty = [l for l in lines if l.strip()]
    for line in non_empty[:3]:
        if is_pallavi_line(line):
            return line
    return None


def write_composition(filepath, lines):
    """Write composition to file with UTF-8 encoding and CRLF line endings."""
    content = '\r\n'.join(lines) + '\r\n'
    with open(filepath, 'w', encoding='utf-8', newline='') as f:
        f.write(content)


def main():
    saints_processed = 0
    total_written = 0
    total_skipped = 0
    skip_reasons = {}

    saint_dirs = sorted(os.listdir(SOURCE_DIR))

    for saint_name in saint_dirs:
        saint_path = os.path.join(SOURCE_DIR, saint_name)
        if not os.path.isdir(saint_path):
            continue

        comps_file = os.path.join(saint_path, 'comps.txt')
        if not os.path.exists(comps_file):
            print(f"  [SKIP] {saint_name}: no comps.txt")
            continue

        # Determine output folder
        folder_name = saint_to_folder_name(saint_name)
        output_folder = os.path.join(OUTPUT_DIR, folder_name)
        os.makedirs(output_folder, exist_ok=True)

        # Get existing filenames to avoid conflicts
        existing_filenames = set(os.listdir(output_folder))

        # Parse compositions
        compositions = parse_compositions(comps_file)

        saint_written = 0
        saint_skipped = 0

        for comp_number, lines in compositions:
            # Check skip conditions
            skip, reason = should_skip(lines)
            if skip:
                total_skipped += 1
                saint_skipped += 1
                skip_reasons[reason] = skip_reasons.get(reason, 0) + 1
                continue

            pallavi = get_pallavi(lines)
            if not pallavi:
                total_skipped += 1
                saint_skipped += 1
                skip_reasons['no pallavi'] = skip_reasons.get('no pallavi', 0) + 1
                continue

            # Build title (first line of file) = pallavi without marker
            title = strip_pallavi_marker(pallavi)

            # Generate filename
            filename = pallavi_to_filename(pallavi, comp_number, existing_filenames)
            existing_filenames.add(filename)

            # Build file content: title on first line, then all composition lines
            file_lines = [title] + lines

            # Write file
            filepath = os.path.join(output_folder, filename)
            write_composition(filepath, file_lines)

            saint_written += 1
            total_written += 1

        saints_processed += 1
        if saint_written > 0 or saint_skipped > 0:
            print(f"  {saint_name} -> {folder_name}: {saint_written} written, {saint_skipped} skipped")

    print()
    print("=" * 60)
    print(f"Saints processed: {saints_processed}")
    print(f"Total compositions written: {total_written}")
    print(f"Total compositions skipped: {total_skipped}")
    if skip_reasons:
        print("Skip reasons:")
        for reason, count in sorted(skip_reasons.items(), key=lambda x: -x[1]):
            print(f"  {reason}: {count}")


if __name__ == '__main__':
    main()
