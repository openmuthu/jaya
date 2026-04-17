/**
 * Script converters: ITRANS ↔ Devanagari ↔ Kannada ↔ Telugu
 * Ported from the Java implementation in org.jaya.scriptconverter
 */

// ── Maps ─────────────────────────────────────────────────────────────────────

const ITRANS_TO_DEVA = {
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
};

const DEVA_TO_ITRANS = {
  '\u0905':'a','\u0906':'A','\u0907':'i','\u0908':'I',
  '\u0909':'u','\u090A':'U','\u090B':'R','\u0960':'RR',
  '\u090C':'LLi','\u0961':'LLI',
  '\u090E':'e','\u090F':'E','\u0910':'ai',
  '\u0912':'o','\u0913':'O','\u0914':'au',
  '\u0902':'M','\u0903':'H',
  '\u093E':'A','\u093F':'i','\u0940':'I','\u0941':'u','\u0942':'U',
  '\u0943':'R','\u0944':'RR','\u0946':'e','\u0947':'E','\u0948':'ai',
  '\u094A':'o','\u094B':'O','\u094C':'au',
  '\u0915':'k','\u0916':'kh','\u0917':'g','\u0918':'gh','\u0919':'~N',
  '\u091A':'ch','\u091B':'Ch','\u091C':'j','\u091D':'jh','\u091E':'~n',
  '\u091F':'T','\u0920':'Th','\u0921':'D','\u0922':'Dh','\u0923':'N',
  '\u0924':'t','\u0925':'th','\u0926':'d','\u0927':'dh','\u0928':'n',
  '\u092A':'p','\u092B':'ph','\u092C':'b','\u092D':'bh','\u092E':'m',
  '\u092F':'y','\u0930':'r','\u0932':'l','\u0933':'L','\u0935':'v',
  '\u0936':'sh','\u0937':'Sh','\u0938':'s','\u0939':'h',
  '\u0915\u094D\u0937':'x',
  '\u0950':'OM','\u093D':'.a','\u094D':''
};

const ITRANS_TO_KANNADA = {
  'a':'\u0C85','A':'\u0C86','aa':'\u0C86',
  'i':'\u0C87','ii':'\u0C88','I':'\u0C88',
  'u':'\u0C89','U':'\u0C8A','uu':'\u0C8A',
  'R':'\u0C8B','Ri':'\u0C8B','Ru':'\u0C8B','RRi':'\u0C8B','R^i':'\u0C8B',
  'RR':'\u0CE0','RRI':'\u0CE0','R^I':'\u0CE0',
  'LLi':'\u0C8C','L^i':'\u0C8C','LLI':'\u0CE1','L^I':'\u0CE1',
  'e':'\u0C8E','E':'\u0C8F','ai':'\u0C90',
  'o':'\u0C92','O':'\u0C93','au':'\u0C94','ou':'\u0C94',
  'M':'\u0C82','H':'\u0C83',':':'\u0C83',
  'k':'\u0C95','K':'\u0C96','kh':'\u0C96','Kh':'\u0C96',
  'g':'\u0C97','G':'\u0C98','gh':'\u0C98','Gh':'\u0C98',
  '~N':'\u0C99',
  'c':'\u0C9A','ch':'\u0C9A','C':'\u0C9B','Ch':'\u0C9B',
  'j':'\u0C9C','J':'\u0C9D','jh':'\u0C9D','Jh':'\u0C9D',
  '~n':'\u0C9E','jn':'\u0C9C\u0CCD\u0C9E',
  'T':'\u0C9F','Th':'\u0CA0','D':'\u0CA1','Dh':'\u0CA2','N':'\u0CA3',
  't':'\u0CA4','th':'\u0CA5','d':'\u0CA6','dh':'\u0CA7','n':'\u0CA8',
  'p':'\u0CAA','P':'\u0CAB','ph':'\u0CAB','Ph':'\u0CAB',
  'b':'\u0CAC','B':'\u0CAD','bh':'\u0CAD','Bh':'\u0CAD',
  'm':'\u0CAE','y':'\u0CAF','r':'\u0CB0','l':'\u0CB2','L':'\u0CB3',
  'v':'\u0CB5','w':'\u0CB5',
  'sh':'\u0CB6','Sh':'\u0CB7','S':'\u0CB7','s':'\u0CB8','h':'\u0CB9',
  'x':'\u0C95\u0CCD\u0CB7','OM':'\u0C93\u0C82','.a':'\u0CBD','.':'.'
};

const ITRANS_TO_TELUGU = {
  'a':'\u0C05','A':'\u0C06','aa':'\u0C06',
  'i':'\u0C07','ii':'\u0C08','I':'\u0C08',
  'u':'\u0C09','U':'\u0C0A','uu':'\u0C0A',
  'R':'\u0C0B','Ri':'\u0C0B','Ru':'\u0C0B','RRi':'\u0C0B','R^i':'\u0C0B',
  'RR':'\u0C60','RRI':'\u0C60','R^I':'\u0C60',
  'LLi':'\u0C0C','L^i':'\u0C0C','LLI':'\u0C61','L^I':'\u0C61',
  'e':'\u0C0E','E':'\u0C0F','ai':'\u0C10',
  'o':'\u0C12','O':'\u0C13','au':'\u0C14','ou':'\u0C14',
  'M':'\u0C02','H':'\u0C03',':':'\u0C03',
  'k':'\u0C15','K':'\u0C16','kh':'\u0C16','Kh':'\u0C16',
  'g':'\u0C17','G':'\u0C18','gh':'\u0C18','Gh':'\u0C18',
  '~N':'\u0C19',
  'c':'\u0C1A','ch':'\u0C1A','C':'\u0C1B','Ch':'\u0C1B',
  'j':'\u0C1C','J':'\u0C1D','jh':'\u0C1D','Jh':'\u0C1D',
  '~n':'\u0C1E','jn':'\u0C1C\u0C4D\u0C1E',
  'T':'\u0C1F','Th':'\u0C20','D':'\u0C21','Dh':'\u0C22','N':'\u0C23',
  't':'\u0C24','th':'\u0C25','d':'\u0C26','dh':'\u0C27','n':'\u0C28',
  'p':'\u0C2A','P':'\u0C2B','ph':'\u0C2B','Ph':'\u0C2B',
  'b':'\u0C2C','B':'\u0C2D','bh':'\u0C2D','Bh':'\u0C2D',
  'm':'\u0C2E','y':'\u0C2F','r':'\u0C30','l':'\u0C32','L':'\u0C33',
  'v':'\u0C35','w':'\u0C35',
  'sh':'\u0C36','Sh':'\u0C37','S':'\u0C37','s':'\u0C38','h':'\u0C39',
  'x':'\u0C15\u0C4D\u0C37','OM':'\u0C50','.a':'\u0C3D','.':'.'
};

// ── Generic ITRANS → Target converter ────────────────────────────────────────

function itransToScript(itrans, map, isConsFn, isVowFn, halantChar) {
  let result = '';
  let pos = 0;
  const len = itrans.length;
  let prevConsonant = false;

  while (pos < len) {
    let targetChar = null;
    let matchLen = 0;
    let skip = false;

    // Greedy longest match
    for (let l = Math.min(4, len - pos); l >= 1; l--) {
      const key = itrans.substring(pos, pos + l);
      if (map[key] !== undefined) {
        targetChar = map[key];
        matchLen = l;
        break;
      }
    }

    if (targetChar === null) {
      if (prevConsonant) { result += halantChar; prevConsonant = false; }
      result += itrans[pos];
      pos++;
      continue;
    }

    const firstCh = targetChar[0];
    const isCurCons  = isConsFn(firstCh);
    const isCurVowel = isVowFn(firstCh);

    if (prevConsonant) {
      if (isCurCons) {
        result += halantChar;
      } else if (isCurVowel) {
        const n = firstCh.codePointAt(0);
        if (n !== isVowFn._baseVowel) {
          targetChar = String.fromCodePoint(n + 56); // dependent vowel form
        } else {
          skip = true;
        }
      }
    }

    prevConsonant = isCurCons;
    if (!skip) result += targetChar;
    pos += matchLen;
  }

  if (prevConsonant) result += halantChar;
  return result;
}

// ── Devanagari helpers ────────────────────────────────────────────────────────

function isDevaCons(ch)  { const n = ch.codePointAt(0); return n >= 0x0915 && n <= 0x0939; }
function isDevaVowel(ch) { const n = ch.codePointAt(0); return n >= 0x0905 && n <= 0x0914; }
isDevaVowel._baseVowel = 0x0905;

function isKannCons(ch)  { const n = ch.codePointAt(0); return n >= 0x0C95 && n <= 0x0CB9; }
function isKannVowel(ch) { const n = ch.codePointAt(0); return n >= 0x0C85 && n <= 0x0C94; }
isKannVowel._baseVowel = 0x0C85;

function isTelCons(ch)  { const n = ch.codePointAt(0); return n >= 0x0C15 && n <= 0x0C39; }
function isTelVowel(ch) { const n = ch.codePointAt(0); return n >= 0x0C05 && n <= 0x0C14; }
isTelVowel._baseVowel = 0x0C05;

// ── Public converters ─────────────────────────────────────────────────────────

export function itransToDevanagari(text) {
  return itransToScript(text, ITRANS_TO_DEVA, isDevaCons, isDevaVowel, '\u094D');
}

export function itransToKannada(text) {
  return itransToScript(text, ITRANS_TO_KANNADA, isKannCons, isKannVowel, '\u0CCD');
}

export function itransToTelugu(text) {
  return itransToScript(text, ITRANS_TO_TELUGU, isTelCons, isTelVowel, '\u0C4D');
}

export function devanagariToITRANS(text) {
  let result = '';
  let pos = 0;
  const len = text.length;

  while (pos < len) {
    // Try 3-char match first (e.g. क्ष), then 2-char, then 1
    let itransChar = null;
    let matchLen = 1;

    for (let l = Math.min(3, len - pos); l >= 1; l--) {
      const key = text.substring(pos, pos + l);
      if (DEVA_TO_ITRANS[key] !== undefined) {
        itransChar = DEVA_TO_ITRANS[key];
        matchLen = l;
        break;
      }
    }

    if (itransChar === null) {
      result += text[pos];
      pos++;
      continue;
    }

    result += itransChar;
    pos += matchLen;

    // If prev char was a consonant and next is not a dependent vowel or halant, add 'a'
    if (
      matchLen === 1 &&
      isDevaCons(text[pos - matchLen]) &&
      (pos >= len || (!isDependentVowelOrHalant(text[pos])))
    ) {
      result += 'a';
    }
  }

  return result;
}

function isDependentVowelOrHalant(ch) {
  if (!ch) return false;
  const n = ch.codePointAt(0);
  return (n >= 0x093E && n <= 0x094F) || n === 0x094D;
}

export function devanagariToKannada(text) {
  return itransToKannada(devanagariToITRANS(text));
}

export function devanagariToTelugu(text) {
  return itransToTelugu(devanagariToITRANS(text));
}

// Kannada/Telugu → Devanagari via code-point shift (same Unicode block layout)
export function kannadaToDevanagari(text) {
  return [...text].map(ch => {
    const n = ch.codePointAt(0);
    return (n >= 0x0C00 && n <= 0x0CFF) ? String.fromCodePoint(n - 0x0300) : ch;
  }).join('');
}

export function teluguToDevanagari(text) {
  return [...text].map(ch => {
    const n = ch.codePointAt(0);
    return (n >= 0x0C00 && n <= 0x0C7F) ? String.fromCodePoint(n - 0x0300) : ch;
  }).join('');
}

/**
 * Convert Devanagari text to the requested display script.
 * @param {string} text  – Devanagari source
 * @param {string} script – 'devanagari' | 'itrans' | 'kannada' | 'telugu'
 */
export function convertScript(text, script) {
  switch (script) {
    case 'itrans':    return devanagariToITRANS(text);
    case 'kannada':   return devanagariToKannada(text);
    case 'telugu':    return devanagariToTelugu(text);
    default:          return text;
  }
}

/**
 * Normalize any supported input script to Devanagari.
 */
export function toDevanagari(text, sourceScript) {
  switch (sourceScript) {
    case 'itrans':    return itransToDevanagari(text);
    case 'kannada':   return kannadaToDevanagari(text);
    case 'telugu':    return teluguToDevanagari(text);
    default:          return text;
  }
}
