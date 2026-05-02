#!/usr/bin/env python3
"""
Fix verse numbering in AgamAH texts to use <chapter>.<verse> format.

Handles multiple source formats found across the downloaded texts:
  1. ।। N ।।  →  ।। C.N ।।   (chapter prefix missing)
  2. ।। १.१ ।।  →  ।। 1.1 ।।  (Devanagari digits → Arabic)
  3. ।।⎵N at EOL  →  ।। C.N ।।  (paramapuruShasaMhitA style)
  4. . N at EOL   →  ।। C.N ।।  (puruShOttamasaMhitA style)
  5. || ज्ञा.सा.सं_N,C.V ||  →  ।। C.V ।।  (jnAnAmRtasArasaMhitA style)
"""
import re, os, sys

AGAMA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'AgamAH')

DEVA_TO_ARAB = str.maketrans('०१२३४५६७८९', '0123456789')

# Longest-match first so एकादश (11) beats दश (10) etc.
ORDINALS = [
    ('एकचत्वारिंश', 41), ('द्विचत्वारिंश', 42), ('चत्वारिंश', 40),
    ('एकोनचत्वारिंश', 39), ('अष्टात्रिंश', 38), ('अष्टत्रिंश', 38), ('सप्तत्रिंश', 37),
    ('षट्त्रिंश', 36), ('पञ्चत्रिंश', 35), ('चतुस्त्रिंश', 34),
    ('त्रयस्त्रिंश', 33), ('द्वात्रिंश', 32), ('एकत्रिंश', 31),
    ('त्रिंश', 30), ('एकोनत्रिंश', 29), ('अष्टाविंश', 28),
    ('सप्तविंश', 27), ('षड्विंश', 26), ('पञ्चविंश', 25),
    ('चतुर्विंश', 24), ('त्रयोविंश', 23), ('द्वाविंश', 22),
    ('एकविंश', 21), ('विंश', 20), ('एकोनविंश', 19),
    ('अष्टादश', 18), ('सप्तदश', 17), ('सप्तदेश', 17), ('षोडश', 16),
    ('पञ्चदश', 15), ('चतुर्दश', 14), ('त्रयोदश', 13),
    ('द्वादश', 12), ('एकादश', 11), ('दशम', 10),
    ('नवम', 9), ('अष्टम', 8), ('सप्तम', 7),
    ('षष्ठ', 6), ('पञ्चम', 5), ('चतुर्थ', 4),
    ('तृतीय', 3), ('द्वितीय', 2), ('प्रथम', 1),
]

CHAPTER_KW = re.compile(r'(अध्याय|ध्याय|पटल|परिच्छेद|प्रकरण|खण्ड)')


def is_chapter_header(line: str) -> bool:
    s = line.strip()
    if not s or len(s) > 100:
        return False
    if s.startswith('[') or '।।' in s or '||' in s:
        return False
    return bool(CHAPTER_KW.search(s))


def extract_chapter_num(text: str):
    s = re.sub(r'^[\(\[]\d+[\)\]]\s*', '', text.strip())  # strip leading footnote markers
    # Devanagari ordinals take priority (they're unambiguous)
    for word, num in ORDINALS:
        if word in s:
            return num
    # Explicit Arabic number after space/dash separator: "पटलः - 3" or "अध्यायः 5"
    # Negative lookbehind: don't match "2-1" (digit before dash)
    m = re.search(r'(?<!\d)[-–]\s*(\d+)', s)
    if m:
        return int(m.group(1))
    return None


# ── helpers ────────────────────────────────────────────────────────────────

def _chap_scanner(lines):
    """Yield (line_index, chapter_number, original_line) tracking chapter changes."""
    chapter = 1
    for i, line in enumerate(lines):
        if is_chapter_header(line):
            n = extract_chapter_num(line.strip())
            if n is not None:
                chapter = n
        yield i, chapter, line


# ── fix functions ───────────────────────────────────────────────────────────

def fix_missing_chapter(content: str) -> str:
    """Files with ।। N ।।  →  ।। C.N ।।"""
    lines = content.split('\n')
    out = []
    for _, chap, line in _chap_scanner(lines):
        new = re.sub(
            r'।।\s*(\d+)\s*।।',
            lambda m, c=chap: f'।। {c}.{m.group(1)} ।।',
            line
        )
        out.append(new)
    return '\n'.join(out)


def fix_deva_digits(content: str) -> str:
    """Files with Devanagari digits in verse numbers: ।। १.१ ।।  →  ।। 1.1 ।।"""
    def repl(m):
        inner = m.group(1).translate(DEVA_TO_ARAB).strip()
        # Collapse spaces inside number tokens: "1 . 1 0" → "1.10"
        inner = re.sub(r'(\d)\s*\.\s*(\d)', r'\1.\2', inner)
        inner = re.sub(r'(\d)\s+(\d)', r'\1\2', inner)
        return f'।। {inner} ।।'
    # Match dandas containing at least one Devanagari digit
    return re.sub(r'।।([^।\n]*[०-९][^।\n]*)।।', repl, content)


def fix_trailing_number(content: str) -> str:
    """Files where verse ends with ।।⎵N (no closing dandas): paramapuruShasaMhitA style."""
    lines = content.split('\n')
    out = []
    for _, chap, line in _chap_scanner(lines):
        new = re.sub(
            r'।।[\s\t]+(\d+)\s*$',
            lambda m, c=chap: f'।। {c}.{m.group(1)} ।।',
            line
        )
        out.append(new)
    return '\n'.join(out)


def fix_period_number(content: str) -> str:
    """Files where verse ends with '. N': puruShOttamasaMhitA style."""
    lines = content.split('\n')
    out = []
    for _, chap, line in _chap_scanner(lines):
        # Match a period followed by a number at end of line
        # but NOT inside a word abbreviation (preceded by a letter)
        new = re.sub(
            r'(?<=[^\w])\.\s+(\d+)\s*$|^\.\s*(\d+)\s*$',
            lambda m, c=chap: f'।। {c}.{m.group(1) or m.group(2)} ।।',
            line
        )
        # Also handle lines that end with just ". N" (no preceding char check)
        if new == line:
            new = re.sub(
                r'\.\s+(\d+)\s*$',
                lambda m, c=chap: f'।। {c}.{m.group(1)} ।।',
                line
            )
        out.append(new)
    return '\n'.join(out)


def fix_deva_dash(content: str) -> str:
    """ahirbudhnyasaMhitA: ।। ३७-१ ।।  →  ।। 37.1 ।।  (Devanagari chapter-dash-verse)"""
    def repl(m):
        inner = m.group(1).translate(DEVA_TO_ARAB).strip()
        # Convert chapter-dash-verse to chapter.verse
        inner = re.sub(r'(\d+)-(\d+)', r'\1.\2', inner)
        return f'।। {inner} ।।'
    return re.sub(r'।।([^।\n]*\d[^।\n]*)।।', repl, content)


def fix_missing_chapter_ranges(content: str) -> str:
    """Add chapter prefix to verse ranges like ।। 5-6 ।।  →  ।। C.5-6 ।।"""
    lines = content.split('\n')
    out = []
    for _, chap, line in _chap_scanner(lines):
        new = re.sub(
            r'।।\s*(\d+(?:-\d+)?)\s*।।',
            lambda m, c=chap: f'।। {c}.{m.group(1)} ।।',
            line
        )
        out.append(new)
    return '\n'.join(out)


def normalize_verse_format(content: str) -> str:
    """Normalize whitespace within chapter.verse markers: '1. 41' → '1.41', '11 .11' → '11.11'"""
    def repl(m):
        inner = m.group(1)
        # Remove spaces/tabs around the dot
        inner = re.sub(r'(\d)\s*[\.\t]\s*(\d)', r'\1.\2', inner)
        # Remove embedded tabs/spaces in multi-digit numbers: "13\t7" → "137"
        inner = re.sub(r'(\d)[\t ](\d)', r'\1\2', inner)
        inner = inner.strip()
        return f'।। {inner} ।।'
    return re.sub(r'।।([^।\n]*\d[^।\n]*)।।', repl, content)


def fix_double_danda_missing_chapter(content: str) -> str:
    """Files using ॥ N ॥ (Devanagari double-danda + Devanagari digits, no chapter prefix).
    Converts to ।। C.N ।। tracking chapter from patala/adhyāya headers.
    e.g. hayashIrShapaMcarAtram: ॥ १ ॥  →  ।। 1.1 ।।
    """
    lines = content.split('\n')
    out = []
    for _, chap, line in _chap_scanner(lines):
        # Convert ॥ N ॥ (Devanagari digits) → ।। C.N ।।
        def repl(m, c=chap):
            n = m.group(1).translate(DEVA_TO_ARAB).strip()
            # collapse any spaces inside multi-digit number
            n = re.sub(r'(\d)\s+(\d)', r'\1\2', n)
            return f'।। {c}.{n} ।।'
        line = re.sub(r'॥\s*([०-९\s]+)\s*॥', repl, line)
        out.append(line)
    return '\n'.join(out)


def fix_prakasha_samhita(content: str) -> str:
    """prakAshasaMhitA: C.V → P.C.V (add pariccheda prefix), fix OCR artifacts.

    The file has two pariccheda sections (प्रथम=1, द्वितीय=2).
    Verse markers currently use adhyāya.verse (C.V); we prepend the pariccheda
    number so they become P.C.V, matching the 3-part style of the reference file.
    Also fixes:
      - OCR artifact र् inside numbers: ।। 3.र्13 ।। → ।। 1.3.13 ।।
      - Plain verse number (missing chapter): ।। 43 ।। → ।। P.C.43 ।।
    """
    PARICCHEDA_1 = 'प्रथमपरिच्छेदः'
    PARICCHEDA_2 = 'द्वितीयपरिच्छेदः'

    lines = content.split('\n')
    out = []
    pariccheda = 1
    chapter = 1

    for line in lines:
        s = line.strip()

        # Track pariccheda
        if s == PARICCHEDA_1:
            pariccheda = 1
            chapter = 1
        elif s == PARICCHEDA_2:
            pariccheda = 2
            chapter = 1

        # Track chapter from headers
        if is_chapter_header(line):
            n = extract_chapter_num(line.strip())
            if n is not None:
                chapter = n

        # Also update chapter from existing C.V verse numbers (more reliable than headers)
        for vm in re.finditer(r'।। (\d+)\.(\d+) ।।', line):
            chapter = int(vm.group(1))

        # Fix OCR artifact: र् inside verse numbers
        line = re.sub(r'(।।[^।\n]*)र्(\d)', r'\1\2', line)

        # Add pariccheda prefix to all ।। ... ।। markers
        def _add_prefix(m, p=pariccheda, c=chapter):
            inner = m.group(1).strip()
            dots = inner.count('.')
            if dots >= 2:
                return m.group(0)          # already P.C.V, leave alone
            if dots == 1:
                return f'।। {p}.{inner} ।।'   # C.V → P.C.V
            # Plain number — missing chapter
            return f'।। {p}.{c}.{inner} ।।'   # N → P.C.N

        line = re.sub(r'।। ([^।\n]+?) ।।', _add_prefix, line)
        out.append(line)

    return '\n'.join(out)


def fix_jnana_amrita(content: str) -> str:
    """jnAnAmRtasArasaMhitA: || ज्ञा.सा.सं_N,C.V || → ।। C.V ।।"""
    def repl(m):
        nums = m.group(1).translate(DEVA_TO_ARAB)
        # format: "N,C.V" or "C.V"
        parts = re.split(r',', nums)
        cv = parts[-1].strip()  # last part is "C.V"
        # Normalize: remove any spaces inside "C . V"
        cv = re.sub(r'(\d)\s*\.\s*(\d)', r'\1.\2', cv)
        cv = re.sub(r'(\d)\s+(\d)', r'\1\2', cv)
        return f'।। {cv} ।।'
    # Match || ... सं_NUMS || (Devanagari + ascii digits, comma, dot)
    return re.sub(r'\|\|[^|]*सं_([०-९\d,\.]+)\s*\|\|', repl, content)


# ── per-file dispatch ───────────────────────────────────────────────────────

DISPATCH = {
    'aniruddhasaMhitA.txt':      fix_missing_chapter,
    'jayAkhyasaMhitA.txt':       fix_missing_chapter,
    'lakShmItantram.txt':        fix_missing_chapter,
    'sAttvatasaMhitA.txt':       fix_missing_chapter_ranges,  # also handles ranges 5-6 → C.5-6
    'viShvaksenasaMhitA.txt':    fix_missing_chapter,
    'prakAshasaMhitA.txt':       fix_prakasha_samhita,
    'paramapuruShasaMhitA.txt':  fix_trailing_number,
    'puruShOttamasaMhitA.txt':   fix_period_number,
    'jnAnAmRtasArasaMhitA.txt':  fix_jnana_amrita,
    # Files with mixed chapter.verse + plain verse numbering from Wikisource
    'ahirbudhnyasaMhitA.txt':    fix_deva_dash,        # ।। ३७-१ ।।  →  ।। 37.1 ।।
    'bhArgavatantram.txt':       fix_missing_chapter,
    'padmasaMhitA.txt':          fix_missing_chapter,
    'vishvAmitrasaMhitA.txt':    fix_missing_chapter,
    # Files needing only whitespace normalization within verse markers
    'paramEshvarasaMhitA.txt':   fix_missing_chapter,   # also has 2 plain verse nums
    # kriyAkairavacandrikA.txt — no verse numbers; skip
    'hayashIrShapaMcarAtram.txt': fix_double_danda_missing_chapter,
}


def process(fname: str, fix_fn, dry_run: bool = False):
    path = os.path.join(AGAMA_DIR, fname)
    with open(path, encoding='utf-16') as f:
        original = f.read()

    fixed = fix_fn(original)

    if fixed == original:
        print(f'  (no changes needed)')
        return

    # Count replacements
    orig_verses = len(re.findall(r'।।[^।\n]+।।', original))
    fixed_verses = len(re.findall(r'।।[^।\n]+।।', fixed))
    print(f'  verse markers: {orig_verses} → {fixed_verses}')

    # Show a few samples
    orig_lines  = [l for l in original.split('\n') if re.search(r'।।[^।\n]+।।', l)]
    fixed_lines = [l for l in fixed.split('\n')    if re.search(r'।।[^।\n]+।।', l)]
    for i in range(min(3, len(orig_lines))):
        o = re.search(r'।।[^।\n]+।।', orig_lines[i])
        n = re.search(r'।।[^।\n]+।।', fixed_lines[i]) if i < len(fixed_lines) else None
        if o and n and o.group() != n.group():
            print(f'  e.g. {o.group()!r}  →  {n.group()!r}')

    if not dry_run:
        with open(path, 'w', encoding='utf-16') as f:
            f.write(fixed)
        print(f'  ✓ written')
    else:
        print(f'  (dry-run, not written)')


def main():
    dry_run = '--dry-run' in sys.argv
    targets = [a for a in sys.argv[1:] if not a.startswith('--')]

    for fname, fix_fn in DISPATCH.items():
        if targets and not any(t.lower() in fname.lower() for t in targets):
            continue
        print(f'\n{fname}')
        process(fname, fix_fn, dry_run=dry_run)

    print('\nDone.')


if __name__ == '__main__':
    os.chdir(AGAMA_DIR)
    main()
