const express = require('express');
const fs = require('fs');
const path = require('path');
const iconv = require('iconv-lite');

const app = express();
const PORT = process.env.PORT || 3000;
const BASE_DIR = path.resolve(__dirname, '..');

app.use(express.static(__dirname));
app.use(express.json());

// ── Security helper ──────────────────────────────────────────────────────────
function safePath(rel) {
  const abs = path.resolve(BASE_DIR, rel.replace(/^\/+/, ''));
  if (!abs.startsWith(BASE_DIR + path.sep) && abs !== BASE_DIR) return null;
  // Disallow anything inside the app subfolder
  const appDir = path.resolve(__dirname);
  if (abs.startsWith(appDir)) return null;
  return abs;
}

// ── Read a UTF-16 LE text file ───────────────────────────────────────────────
function readTextFile(absPath) {
  const buf = fs.readFileSync(absPath);
  // Detect BOM: UTF-16 LE starts with FF FE
  if (buf[0] === 0xff && buf[1] === 0xfe) {
    return iconv.decode(buf, 'utf-16le');
  }
  // Try UTF-16 LE without BOM (most files here)
  try {
    return iconv.decode(buf, 'utf-16le');
  } catch {
    return buf.toString('utf8');
  }
}

// ── Build directory tree ─────────────────────────────────────────────────────
function buildTree(dir, relPath) {
  let entries;
  try {
    entries = fs.readdirSync(dir, { withFileTypes: true });
  } catch {
    return null;
  }

  const children = [];

  for (const entry of entries) {
    if (entry.name.startsWith('.')) continue;
    // Skip the app folder itself at root level
    if (relPath === '' && entry.name === 'app') continue;

    const childRel = relPath ? `${relPath}/${entry.name}` : entry.name;

    if (entry.isDirectory()) {
      const subtree = buildTree(path.join(dir, entry.name), childRel);
      if (subtree) children.push(subtree);
    } else if (entry.name.endsWith('.txt')) {
      children.push({ name: entry.name, path: childRel, type: 'file' });
    }
  }

  // Dirs before files, then alphabetical
  children.sort((a, b) => {
    if (a.type !== b.type) return a.type === 'dir' ? -1 : 1;
    return a.name.localeCompare(b.name, undefined, { sensitivity: 'base' });
  });

  return { name: path.basename(dir) || relPath, path: relPath, type: 'dir', children };
}

// ── Cache for file contents (Devanagari text) ────────────────────────────────
const fileCache = new Map();
const MAX_CACHE = 50;

function getCachedContent(absPath) {
  if (fileCache.has(absPath)) return fileCache.get(absPath);
  const content = readTextFile(absPath);
  if (fileCache.size >= MAX_CACHE) {
    fileCache.delete(fileCache.keys().next().value);
  }
  fileCache.set(absPath, content);
  return content;
}

// ── Script conversion (Devanagari ↔ ITRANS ↔ Kannada/Telugu) ────────────────
// Included inline to avoid a separate round-trip; also exported via /static

// ── API: file tree ───────────────────────────────────────────────────────────
app.get('/api/tree', (_req, res) => {
  const tree = buildTree(BASE_DIR, '');
  res.json(tree);
});

// ── API: file content ────────────────────────────────────────────────────────
app.get('/api/file', (req, res) => {
  const rel = req.query.path;
  if (!rel) return res.status(400).json({ error: 'path required' });

  const abs = safePath(rel);
  if (!abs) return res.status(403).json({ error: 'Access denied' });

  try {
    const content = getCachedContent(abs);
    res.json({ content });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// ── ITRANS → Devanagari (server-side for search normalization) ───────────────
const ITRANS_TO_DEVA = {
  'a': '\u0905', 'A': '\u0906', 'aa': '\u0906',
  'i': '\u0907', 'ii': '\u0908', 'I': '\u0908',
  'u': '\u0909', 'U': '\u090A', 'uu': '\u090A',
  'R': '\u090B', 'Ri': '\u090B', 'Ru': '\u090B', 'RRi': '\u090B', 'R^i': '\u090B',
  'RR': '\u0960', 'RRI': '\u0960', 'R^I': '\u0960',
  'LLi': '\u090C', 'L^i': '\u090C', 'LLI': '\u0961', 'L^I': '\u0961',
  'e': '\u090E', 'E': '\u090F', 'ai': '\u0910',
  'o': '\u0912', 'O': '\u0913', 'au': '\u0914', 'ou': '\u0914',
  'M': '\u0902', 'H': '\u0903', ':': '\u0903',
  'k': '\u0915', 'K': '\u0916', 'kh': '\u0916', 'Kh': '\u0916',
  'g': '\u0917', 'G': '\u0918', 'gh': '\u0918', 'Gh': '\u0918',
  '~N': '\u0919', 'c': '\u091A', 'ch': '\u091A',
  'C': '\u091B', 'Ch': '\u091B',
  'j': '\u091C', 'J': '\u091D', 'jh': '\u091D', 'Jh': '\u091D',
  '~n': '\u091E', 'jn': '\u091C\u094D\u091E',
  'T': '\u091F', 'Th': '\u0920', 'D': '\u0921', 'Dh': '\u0922', 'N': '\u0923',
  't': '\u0924', 'th': '\u0925', 'd': '\u0926', 'dh': '\u0927', 'n': '\u0928',
  'p': '\u092A', 'P': '\u092B', 'ph': '\u092B', 'Ph': '\u092B',
  'b': '\u092C', 'B': '\u092D', 'bh': '\u092D', 'Bh': '\u092D',
  'm': '\u092E', 'y': '\u092F', 'r': '\u0930', 'l': '\u0932', 'L': '\u0933',
  'v': '\u0935', 'w': '\u0935',
  'sh': '\u0936', 'Sh': '\u0937', 'S': '\u0937', 's': '\u0938', 'h': '\u0939',
  'x': '\u0915\u094D\u0937', 'OM': '\u0950', '.a': '\u093D', '.': '.'
};

function itransToDevanagari(itrans) {
  let result = '';
  let pos = 0;
  const len = itrans.length;
  let prevConsonant = false;

  function isConsonant(ch) { const n = ch.codePointAt(0); return n >= 0x0915 && n <= 0x0939; }
  function isVowel(ch) { const n = ch.codePointAt(0); return n >= 0x0905 && n <= 0x0914; }

  while (pos < len) {
    let sanskritChar = null;
    let skip = false;
    let matchLen = 0;

    // Greedy longest match
    for (let l = Math.min(4, len - pos); l >= 1; l--) {
      const key = itrans.substring(pos, pos + l);
      if (ITRANS_TO_DEVA[key] !== undefined) {
        sanskritChar = ITRANS_TO_DEVA[key];
        matchLen = l;
        break;
      }
    }

    if (sanskritChar === null) {
      if (prevConsonant) { result += '\u094D'; prevConsonant = false; }
      result += itrans[pos];
      pos++;
      continue;
    }

    const curCh = sanskritChar[0];
    const isCurCons = isConsonant(curCh);
    const isCurVowel = isVowel(curCh);

    if (prevConsonant) {
      if (isCurCons) {
        result += '\u094D';
      } else if (isCurVowel) {
        const n = curCh.codePointAt(0);
        if (n !== 0x0905) {
          sanskritChar = String.fromCodePoint(n + 56);
        } else {
          skip = true;
        }
      }
    }

    prevConsonant = isCurCons;
    if (!skip) result += sanskritChar;
    pos += matchLen;
  }
  if (prevConsonant) result += '\u094D';
  return result;
}

// Kannada → Devanagari (via code-point offset)
function kannadaToDevanagari(text) {
  let out = '';
  for (const ch of text) {
    const n = ch.codePointAt(0);
    // Kannada block: 0C00–0CFF → Devanagari block: 0900–097F
    if (n >= 0x0C00 && n <= 0x0CFF) {
      out += String.fromCodePoint(n - 0x0300);
    } else {
      out += ch;
    }
  }
  return out;
}

// Telugu → Devanagari (via code-point offset)
function teluguToDevanagari(text) {
  let out = '';
  for (const ch of text) {
    const n = ch.codePointAt(0);
    // Telugu block: 0C00–0C7F → Devanagari: 0900–097F
    if (n >= 0x0C00 && n <= 0x0C7F) {
      out += String.fromCodePoint(n - 0x0300);
    } else {
      out += ch;
    }
  }
  return out;
}

function normalizeToDevanagari(query, script) {
  switch (script) {
    case 'itrans':    return itransToDevanagari(query);
    case 'kannada':   return kannadaToDevanagari(query);
    case 'telugu':    return teluguToDevanagari(query);
    default:          return query; // already Devanagari
  }
}

// Simple Levenshtein for fuzzy per-word matching
function levenshtein(a, b) {
  if (a === b) return 0;
  const m = a.length, n = b.length;
  if (m === 0) return n;
  if (n === 0) return m;
  const dp = Array.from({ length: m + 1 }, (_, i) => [i, ...Array(n).fill(0)]);
  for (let j = 1; j <= n; j++) dp[0][j] = j;
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      dp[i][j] = a[i-1] === b[j-1]
        ? dp[i-1][j-1]
        : 1 + Math.min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]);
    }
  }
  return dp[m][n];
}

// Collect all .txt files
function collectFiles(dir, relPath, list = []) {
  let entries;
  try { entries = fs.readdirSync(dir, { withFileTypes: true }); } catch { return list; }
  for (const e of entries) {
    if (e.name.startsWith('.')) continue;
    if (relPath === '' && e.name === 'app') continue;
    const childRel = relPath ? `${relPath}/${e.name}` : e.name;
    if (e.isDirectory()) collectFiles(path.join(dir, e.name), childRel, list);
    else if (e.name.endsWith('.txt')) list.push(childRel);
  }
  return list;
}

// ── API: search ──────────────────────────────────────────────────────────────
app.get('/api/search', (req, res) => {
  const {
    q = '',
    script = 'devanagari',
    paths = '',        // comma-separated folder/file filters
    fuzzy = 'false',
    regex = 'false',
    offset = '0',
    limit = '20'
  } = req.query;

  if (!q.trim()) return res.json({ results: [], total: 0 });

  const isFuzzy = fuzzy === 'true';
  const isRegex = regex === 'true';
  const offsetNum = parseInt(offset, 10) || 0;
  const limitNum  = Math.min(parseInt(limit, 10) || 20, 100);

  // Normalize query to Devanagari for searching
  const devaQuery = normalizeToDevanagari(q.trim(), script);

  // Build regex or plain matcher
  let searchFn;
  if (isRegex) {
    let re;
    try { re = new RegExp(devaQuery, 'i'); } catch { return res.status(400).json({ error: 'Invalid regex' }); }
    searchFn = (line) => re.test(line);
  } else if (isFuzzy) {
    const words = devaQuery.split(/\s+/).filter(Boolean);
    const threshold = Math.floor(devaQuery.length * 0.25); // 25% edit distance
    searchFn = (line) => {
      return words.every(w => {
        if (line.includes(w)) return true;
        // Check windows of same length in line
        for (let i = 0; i <= line.length - w.length; i++) {
          if (levenshtein(w, line.substring(i, i + w.length)) <= Math.max(1, Math.floor(w.length * 0.25))) return true;
        }
        return false;
      });
    };
  } else {
    searchFn = (line) => line.includes(devaQuery);
  }

  // Filter files
  let allFiles = collectFiles(BASE_DIR, '');
  if (paths) {
    const filters = paths.split(',').map(p => p.trim()).filter(Boolean);
    allFiles = allFiles.filter(f => filters.some(fp => f === fp || f.startsWith(fp + '/')));
  }

  const allResults = [];

  for (const relFile of allFiles) {
    const abs = safePath(relFile);
    if (!abs) continue;
    let content;
    try { content = getCachedContent(abs); } catch { continue; }

    const lines = content.split(/\r?\n/);
    for (let lineIdx = 0; lineIdx < lines.length; lineIdx++) {
      const line = lines[lineIdx].trim();
      if (!line) continue;
      if (searchFn(line)) {
        allResults.push({
          file: relFile,
          lineNumber: lineIdx + 1,
          lineText: line,
          context: lines.slice(Math.max(0, lineIdx - 1), lineIdx + 2).join('\n')
        });
      }
    }

    if (allResults.length > 2000) break; // safety cap
  }

  const total = allResults.length;
  const page  = allResults.slice(offsetNum, offsetNum + limitNum);

  res.json({ results: page, total, query: q, devaQuery });
});

// ── Serve index for all non-API routes ───────────────────────────────────────
app.get('/', (_req, res) => res.sendFile(path.join(__dirname, 'index.html')));
app.get('/search', (_req, res) => res.sendFile(path.join(__dirname, 'search.html')));

app.listen(PORT, () => {
  console.log(`Jaya Grantha Viewer running at http://localhost:${PORT}`);
});
