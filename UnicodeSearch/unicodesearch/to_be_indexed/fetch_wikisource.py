#!/usr/bin/env python3
"""
Jaya Grantha Viewer — Wikisource Acquisition Script (Phase 1)
Fetches 27 high-priority texts from sa.wikisource.org and saves as UTF-16 LE.

Usage:
  python3 fetch_wikisource.py              # fetch all missing texts
  python3 fetch_wikisource.py --list       # show manifest without fetching
  python3 fetch_wikisource.py --dry-run    # fetch+strip but don't write files
  python3 fetch_wikisource.py PATTERN      # fetch only items whose id matches PATTERN

Already present (skipped automatically):
  - kAshikA (vyAkaraNa/kAshikA-1..8.txt)
  - abhijnAnashAkuntalam (sAhitya/kALidAsa/abhijnAnashAkuntalam.txt)

Special actions:
  python3 fetch_wikisource.py --split-ramayana   # split existing vAlmIki rAmAyaNa.txt
                                                  # into 7 kANDa files
"""

import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request

BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
API_URL   = "https://sa.wikisource.org/w/api.php"
SLEEP_SEC    = 4.0    # polite delay; ~900 req/hr, fine for texts under ~200 sub-pages
MAX_SUBPAGES = 500    # safety cap; texts with more will be truncated with a warning
MAX_RETRIES  = 6      # retries on 429 / transient errors

# ── Manifest ──────────────────────────────────────────────────────────────────
# Each entry:
#   id       – ITRANS identifier used in output filename / filtering
#   label    – human-readable Sanskrit name
#   out      – output path relative to BASE_DIR
#   page     – single Wikisource page title  (use this or 'prefix', not both)
#   prefix   – auto-discover all sub-pages starting with this prefix
#   pages    – explicit ordered list of page titles to concatenate
#
# 'skip_if_exists': True means "don't overwrite an already-present file"
# (default True for all entries)

MANIFEST = [

    # ── Upanishads ────────────────────────────────────────────────────────────
    {
        'id':     'kaTha-upaniShat',
        'label':  'कठोपनिषत्',
        'prefix': 'कठोपनिषत्',            # 6 vallī sub-pages under this prefix
        'out':    'vEda/kaTha-upaniShat.txt',
    },
    {
        'id':    'kEna-upaniShat',
        'label': 'केनोपनिषत्',
        'page':  'केनोपनिषत्',
        'out':   'vEda/kEna-upaniShat.txt',
    },
    {
        'id':    'muMDaka-upaniShat',
        'label': 'मुण्डकोपनिषत्',
        'page':  'मुण्डकोपनिषत्',          # single page on sa.wikisource
        'out':   'vEda/muMDaka-upaniShat.txt',
    },
    {
        'id':    'prashna-upaniShat',
        'label': 'प्रश्नोपनिषत्',
        'prefix': 'प्रश्नोपनिषत्',          # 6 sub-pages (प्रथमः through षष्ठः)
        'out':   'vEda/prashna-upaniShat.txt',
    },
    {
        'id':    'taittirIya-upaniShat',
        'label': 'तैत्तिरीयोपनिषत्',
        'page':  'तैत्तिरीयोपनिषत्',
        'out':   'vEda/taittirIya-upaniShat.txt',
    },
    {
        'id':    'nRsiMhapUrvaTApanIya',
        'label': 'नृसिंहपूर्वतापनीयोपनिषत्',
        'page':  'नृसिंहतापनी उपनिषद्-1',  # verified title
        'out':   'vEda/nRsiMhapUrvaTApanIya.txt',
    },
    {
        'id':    'nRsiMhOttaraTApanIya',
        'label': 'नृसिंहोत्तरतापनीयोपनिषत्',
        'page':  'नृसिंहतापनी उपनिषद्-2',  # verified title
        'out':   'vEda/nRsiMhOttaraTApanIya.txt',
    },
    {
        'id':    'nArAyaNa-upaniShat',
        'label': 'नारायणोपनिषत्',
        'page':  'नारायण उपनिषद्',          # verified title (space, not ·)
        'out':   'vEda/nArAyaNa-upaniShat.txt',
    },
    {
        'id':    'nArAyaNapUrvatApanIya',
        'label': 'नारायणपूर्वतापिनीयोपनिषत्',
        'page':  'नारायणपूर्वतापिनीयोपनिषत्',
        'out':   'vEda/nArAyaNapUrvatApanIya.txt',
    },
    {
        'id':    'nArAyaNOttaraTApanIya',
        'label': 'नारायणोत्तरतापिनीयोपनिषत्',
        'page':  'नारायणोत्तरतापिनीयोपनिषत्',
        'out':   'vEda/nArAyaNOttaraTApanIya.txt',
    },

    # ── Vedic texts ───────────────────────────────────────────────────────────
    {
        'id':    'taittirIyasaMhitA',
        'label': 'तैत्तिरीयसंहिता (कृष्णयजुर्वेद)',
        # Two combined pages covering all 7 kāṇḍas
        'pages': ['तैत्तिरीयसंहिता-१-४', 'तैत्तिरीयसंहिता-५-७'],
        'out':   'vEda/taittirIyasaMhitA.txt',
    },
    {
        'id':    'vAjasanEyisaMhitA',
        'label': 'शुक्लयजुर्वेदसंहिता (वाजसनेयि)',
        'prefix': 'शुक्लयजुर्वेदः',          # 40 adhyāyas as sub-pages
        'out':   'vEda/vAjasanEyisaMhitA.txt',
    },
    # ऐतरेयब्राह्मणम् — not available as standalone on sa.wikisource (skipped)
    {
        'id':    'taittirIyabrAhmaNam',
        'label': 'तैत्तिरीयब्राह्मणम्',
        'page':  'तैत्तिरीयब्राह्मणम्',       # plain text version (single page)
        'out':   'vEda/taittirIyabrAhmaNam.txt',
    },
    {
        'id':    'aitarEyAraNyakam',
        'label': 'ऐतरेय आरण्यकम्',
        'prefix': 'ऐतरेय आरण्यकम्',           # verified: has आरण्यक १-५ sub-pages
        'out':   'vEda/aitarEyAraNyakam.txt',
    },
    {
        'id':    'taittirIyAraNyakam',
        'label': 'तैत्तिरीयारण्यकम्',
        'prefix': 'तैत्तिरीयारण्यकम्(विस्वर)', # verified: 10 प्रपाठक sub-pages
        'out':   'vEda/taittirIyAraNyakam.txt',
    },

    # ── Ramayana / Yoga Vasistha ──────────────────────────────────────────────
    {
        'id':    'adhyAtmarAmAyaNam',
        'label': 'अध्यात्मरामायणम्',
        'page':  'अध्यात्मरामायणम्',
        'out':   'rAmAyaNa/adhyAtmarAmAyaNam.txt',
    },
    {
        'id':    'yOgavAsiShTha',
        'label': 'योगवासिष्ठः',
        'prefix': 'योगवासिष्ठः',               # large; many sargas as sub-pages
        'out':   'rAmAyaNa/yOgavAsiShTha.txt',
    },

    # ── Smritis ───────────────────────────────────────────────────────────────
    {
        'id':    'vasiShThasmRti',
        'label': 'वसिष्ठस्मृतिः',
        'page':  'वसिष्ठस्मृतिः',              # verified title
        'out':   'smRti/vasiShThasmRti.txt',
    },
    {
        'id':    'gautamasmRti',
        'label': 'गौतमधर्मसूत्रम्',
        'page':  'गौतमधर्मसूत्रम्',
        'out':   'smRti/gautamasmRti.txt',
    },

    # ── Grammar ───────────────────────────────────────────────────────────────
    {
        'id':    'aShTAdhyAyI',
        'label': 'अष्टाध्यायी (पाणिनि)',
        # Main + 8 adhyāya sub-pages + gana-pāṭha + dhātu-pāṭha
        'prefix': 'अष्टाध्यायी',
        'out':   'vyAkaraNa/pANini/aShTAdhyAyI.txt',
    },
    {
        'id':    'mahAbhAShyam',
        'label': 'व्याकरणमहाभाष्यम् (पतञ्जलि)',
        'prefix': 'व्याकरणमहाभाष्यम्',          # verified: 8 adhyāya sub-pages
        'out':   'vyAkaraNa/pataMjali/mahAbhAShyam.txt',
    },
    {
        'id':    'siddhAntakaumudI',
        'label': 'सिद्धान्तकौमुदी',
        'prefix': 'सिद्धान्तकौमुदी',             # 7 prakaraṇa sub-pages
        'out':   'vyAkaraNa/siddhAntakaumudI.txt',
    },

    # ── Philosophy / Darshana ─────────────────────────────────────────────────
    {
        'id':    'yOgasUtrANi',
        'label': 'योगसूत्रम् (पतञ्जलि)',
        'prefix': 'योगसूत्रम्',                  # verified: 4 pāda sub-pages
        'out':   'itaragraMthAH/yOgasUtrANi.txt',
    },
    {
        'id':    'nyAyasUtrANi',
        'label': 'न्यायसूत्राणि (गोतम)',
        'prefix': 'न्यायसूत्राणि',               # verified: 5 adhyāya sub-pages
        'out':   'itaragraMthAH/nyAyasUtrANi.txt',
    },
    {
        'id':    'sAMkhyakArikA',
        'label': 'सांख्यकारिका (ईश्वरकृष्ण)',
        'page':  'सांख्यकारिका',
        'out':   'itaragraMthAH/sAMkhyakArikA.txt',
    },

    # ── Pāñcarātra Āgamas ─────────────────────────────────────────────────────
    {
        'id':     'paramEshvarasaMhitA',
        'label':  'परमेश्वरसंहिता (पाञ्चरात्र)',
        'prefix': 'परमेश्वरसंहिता',
        'out':    'AgamAH/paramEshvarasaMhitA.txt',
    },
    {
        'id':     'lakShmItantram',
        'label':  'लक्ष्मीतन्त्रम् (पाञ्चरात्र)',
        'prefix': 'लक्ष्मीतन्त्रम्',
        'out':    'AgamAH/lakShmItantram.txt',
    },
    {
        'id':     'sAttvatasaMhitA',
        'label':  'सात्त्वतसंहिता (पाञ्चरात्र)',
        'prefix': 'सात्त्वतसंहिता',
        'out':    'AgamAH/sAttvatasaMhitA.txt',
    },
    {
        'id':     'jayAkhyasaMhitA',
        'label':  'जयाख्यसंहिता (पाञ्चरात्र)',
        'prefix': 'जयाख्यसंहिता',
        'out':    'AgamAH/jayAkhyasaMhitA.txt',
    },
    {
        'id':     'padmasaMhitA',
        'label':  'पद्मसंहिता (पाञ्चरात्र)',
        # Sub-pages live under pāda titles directly (not under पद्मसंहिता/):
        #   ज्ञानपादः/अध्यायः N (12), योगपादः/अध्यायः N (5),
        #   क्रियापादः/अध्यायः N (32), चर्यापादः/अध्यायः N (33)
        'skip_if_exists': False,  # force re-fetch since old file was just the index
        'pages': [
            'ज्ञानपादः/अध्यायः १', 'ज्ञानपादः/अध्यायः २', 'ज्ञानपादः/अध्यायः ३',
            'ज्ञानपादः/अध्यायः ४', 'ज्ञानपादः/अध्यायः ५', 'ज्ञानपादः/अध्यायः ६',
            'ज्ञानपादः/अध्यायः ७', 'ज्ञानपादः/अध्यायः ८', 'ज्ञानपादः/अध्यायः ९',
            'ज्ञानपादः/अध्यायः १०', 'ज्ञानपादः/अध्यायः ११', 'ज्ञानपादः/अध्यायः १२',
            'योगपादः/अध्यायः १', 'योगपादः/अध्यायः २', 'योगपादः/अध्यायः ३',
            'योगपादः/अध्यायः ४', 'योगपादः/अध्यायः ५',
            'क्रियापादः/अध्यायः १', 'क्रियापादः/अध्यायः २', 'क्रियापादः/अध्यायः ३',
            'क्रियापादः/अध्यायः ४', 'क्रियापादः/अध्यायः ५', 'क्रियापादः/अध्यायः ६',
            'क्रियापादः/अध्यायः ७', 'क्रियापादः/अध्यायः ८', 'क्रियापादः/अध्यायः ९',
            'क्रियापादः/अध्यायः १०', 'क्रियापादः/अध्यायः ११', 'क्रियापादः/अध्यायः १२',
            'क्रियापादः/अध्यायः १३', 'क्रियापादः/अध्यायः १४', 'क्रियापादः/अध्यायः १५',
            'क्रियापादः/अध्यायः १६', 'क्रियापादः/अध्यायः १७', 'क्रियापादः/अध्यायः १८',
            'क्रियापादः/अध्यायः १९', 'क्रियापादः/अध्यायः २०', 'क्रियापादः/अध्यायः २१',
            'क्रियापादः/अध्यायः २२', 'क्रियापादः/अध्यायः २३', 'क्रियापादः/अध्यायः २४',
            'क्रियापादः/अध्यायः २५', 'क्रियापादः/अध्यायः २६', 'क्रियापादः/अध्यायः २७',
            'क्रियापादः/अध्यायः २८', 'क्रियापादः/अध्यायः २९', 'क्रियापादः/अध्यायः ३०',
            'क्रियापादः/अध्यायः ३१', 'क्रियापादः/अध्यायः ३२',
            'चर्यापादः/अध्यायः १', 'चर्यापादः/अध्यायः २', 'चर्यापादः/अध्यायः ३',
            'चर्यापादः/अध्यायः ४', 'चर्यापादः/अध्यायः ५', 'चर्यापादः/अध्यायः ६',
            'चर्यापादः/अध्यायः ७', 'चर्यापादः/अध्यायः ८', 'चर्यापादः/अध्यायः ९',
            'चर्यापादः/अध्यायः १०', 'चर्यापादः/अध्यायः ११', 'चर्यापादः/अध्यायः १२',
            'चर्यापादः/अध्यायः १३', 'चर्यापादः/अध्यायः १४', 'चर्यापादः/अध्यायः १५',
            'चर्यापादः/अध्यायः १६', 'चर्यापादः/अध्यायः १७', 'चर्यापादः/अध्यायः १८',
            'चर्यापादः/अध्यायः १९', 'चर्यापादः/अध्यायः २०', 'चर्यापादः/अध्यायः २१',
            'चर्यापादः/अध्यायः २२', 'चर्यापादः/अध्यायः २३', 'चर्यापादः/अध्यायः २४',
            'चर्यापादः/अध्यायः २५', 'चर्यापादः/अध्यायः २६', 'चर्यापादः/अध्यायः २७',
            'चर्यापादः/अध्यायः २८', 'चर्यापादः/अध्यायः २९', 'चर्यापादः/अध्यायः ३०',
            'चर्यापादः/अध्यायः ३१', 'चर्यापादः/अध्यायः ३२', 'चर्यापादः/अध्यायः ३३',
        ],
        'out':    'AgamAH/padmasaMhitA.txt',
    },
    {
        'id':     'viShNusaMhitA',
        'label':  'विष्णुसंहिता (पाञ्चरात्र)',
        'prefix': 'विष्णुसंहिता',
        'out':    'AgamAH/viShNusaMhitA.txt',
    },
    {
        'id':     'aniruddhasaMhitA',
        'label':  'अनिरुद्धसंहिता (पाञ्चरात्र)',
        'prefix': 'अनिरुद्धसंहिता',
        'out':    'AgamAH/aniruddhasaMhitA.txt',
    },
    {
        'id':     'paramapuruShasaMhitA',
        'label':  'परमपुरुषसंहिता (पाञ्चरात्र)',
        'prefix': 'परमपुरुषसंहिता',
        'out':    'AgamAH/paramapuruShasaMhitA.txt',
    },
    {
        'id':     'puruShOttamasaMhitA',
        'label':  'पुरुषोत्तमसंहिता (पाञ्चरात्र)',
        'prefix': 'पुरुषोत्तमसंहिता',
        'out':    'AgamAH/puruShOttamasaMhitA.txt',
    },
    {
        'id':     'prakAshasaMhitA',
        'label':  'प्रकाशसंहिता (पाञ्चरात्र)',
        'prefix': 'प्रकाशसंहिता',
        'out':    'AgamAH/prakAshasaMhitA.txt',
    },
    {
        'id':     'prashnasaMhitA',
        'label':  'प्रश्नसंहिता (पाञ्चरात्र)',
        'prefix': 'प्रश्नसंहिता',
        'out':    'AgamAH/prashnasaMhitA.txt',
    },
    {
        'id':     'vishvAmitrasaMhitA',
        'label':  'विश्वामित्रसंहिता (पाञ्चरात्र)',
        'prefix': 'विश्वामित्रसंहिता',
        'out':    'AgamAH/vishvAmitrasaMhitA.txt',
    },
    {
        'id':     'ahirbudhnyasaMhitA',
        'label':  'अहिर्बुध्नसंहिता (पाञ्चरात्र)',
        'prefix': 'अहिर्बुध्नसंहिता',
        'out':    'AgamAH/ahirbudhnyasaMhitA.txt',
    },
    {
        'id':     'viShvaksenasaMhitA',
        'label':  'विश्वक्सेनसंहिता (पाञ्चरात्र)',
        # Sub-pages are titled "विश्वक्सेनासंहिता /अध्यायः N" — note ā + space before /
        # The trailing space in prefix means discover_subpages searches for prefix+'/'
        # = 'विश्वक्सेनासंहिता /' which correctly matches all 39 chapter sub-pages.
        'prefix': 'विश्वक्सेनासंहिता ',
        'out':    'AgamAH/viShvaksenasaMhitA.txt',
    },
    {
        'id':     'jnAnAmRtasArasaMhitA',
        'label':  'ज्ञानामृतसारसंहिता / नारदपञ्चरात्रम् (पाञ्चरात्र)',
        'page':   'ज्ञानामृतसारसंहिता',   # entire text on a single page
        'out':    'AgamAH/jnAnAmRtasArasaMhitA.txt',
    },
    {
        'id':     'bhArgavatantram',
        'label':  'भार्गवतन्त्रम् (पाञ्चरात्र)',
        'prefix': 'भार्गवतन्त्रम्',
        'out':    'AgamAH/bhArgavatantram.txt',
    },
    {
        'id':     'AgamapramANyam',
        'label':  'आगमप्रामाण्यम् (यामुनाचार्य)',
        'page':   'आगमप्रामाण्यम्',
        'out':    'AgamAH/AgamapramANyam.txt',
    },
    {
        'id':     'hayashIrShapaMcarAtram',
        'label':  'हयशीर्षपञ्चरात्रम्',
        'page':   'हयशीर्षपञ्चरात्रम्',
        'out':    'AgamAH/hayashIrShapaMcarAtram.txt',
    },
    {
        'id':     'kriyAkairavacandrikA',
        'label':  'क्रियाकैरवचन्द्रिका (पाञ्चरात्र)',
        # Page title on Wikisource has a BOM (U+FEFF) as its first character
        'page':   '\ufeffक्रिया-कैरव-चन्द्रिका',
        'out':    'AgamAH/kriyAkairavacandrikA.txt',
    },
]


# ── Wikisource API helpers ────────────────────────────────────────────────────

def _api_get(params: dict) -> dict:
    params.setdefault('format', 'json')
    params.setdefault('formatversion', '2')
    url = API_URL + '?' + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers={'User-Agent': 'JayaGranthaViewer/1.0'})
    for attempt in range(MAX_RETRIES):
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.load(r)
        except urllib.error.HTTPError as e:
            if e.code == 429:
                wait = SLEEP_SEC * (3 ** attempt)   # 0.8 → 2.4 → 7.2 → 21.6 s
                print(f'    rate-limited, waiting {wait:.1f}s …')
                time.sleep(wait)
            else:
                raise
    raise RuntimeError(f'Failed after {MAX_RETRIES} retries: {url}')


def fetch_wikitext(title: str) -> str:
    """Return raw wikitext for a single page title (follows redirects), or ''."""
    data = _api_get({
        'action': 'query', 'titles': title,
        'prop': 'revisions', 'rvprop': 'content', 'rvslots': 'main',
        'redirects': '1',   # follow redirects automatically
    })
    pages = data.get('query', {}).get('pages', [])
    if not pages:
        return ''
    page = pages[0]
    if page.get('missing'):
        return ''
    revs = page.get('revisions', [])
    if not revs:
        return ''
    # formatversion=2 nests content under slots.main
    slot = revs[0].get('slots', {}).get('main', {})
    return slot.get('content', revs[0].get('content', ''))


def discover_subpages(prefix: str) -> list[str]:
    """Return sorted list of all page titles that start with prefix + '/'."""
    subpages = []
    apcontinue = None
    while True:
        params = {
            'action': 'query', 'list': 'allpages',
            'apprefix': prefix + '/',
            'apnamespace': '0',
            'aplimit': '500',
        }
        if apcontinue:
            params['apcontinue'] = apcontinue
        data = _api_get(params)
        for p in data.get('query', {}).get('allpages', []):
            subpages.append(p['title'])
        cont = data.get('continue', {})
        apcontinue = cont.get('apcontinue')
        if not apcontinue:
            break
        time.sleep(SLEEP_SEC)

    # Sort numerically: Devanagari digits sort alphabetically wrong (१० < २),
    # so extract embedded numbers and compare as integers.
    _DEVA = str.maketrans('०१२३४५६७८९', '0123456789')
    def _num_key(title: str):
        t = title.translate(_DEVA)
        parts = re.split(r'(\d+)', t)
        return [int(p) if p.isdigit() else p for p in parts]
    subpages.sort(key=_num_key)
    if len(subpages) > MAX_SUBPAGES:
        print(f'    WARNING: {len(subpages)} sub-pages found; capping at {MAX_SUBPAGES}')
        subpages = subpages[:MAX_SUBPAGES]
    return subpages


# ── Markup stripper ───────────────────────────────────────────────────────────

def strip_markup(wikitext: str) -> str:
    t = wikitext

    # Remove <noinclude> blocks entirely (navigation headers, categories)
    t = re.sub(r'<noinclude>.*?</noinclude>', '', t, flags=re.DOTALL | re.IGNORECASE)

    # Remove <references/> and <ref>...</ref> footnotes
    t = re.sub(r'<ref[^>]*>.*?</ref>', '', t, flags=re.DOTALL | re.IGNORECASE)
    t = re.sub(r'<references\s*/>', '', t, flags=re.IGNORECASE)

    # Strip <poem> tags but keep content
    t = re.sub(r'</?poem[^>]*>', '', t, flags=re.IGNORECASE)

    # <br> → newline
    t = re.sub(r'<br\s*/?>', '\n', t, flags=re.IGNORECASE)

    # Remove remaining HTML tags (keep their text content)
    t = re.sub(r'<[^>]+>', '', t)

    # Remove {{templates}} — including nested ones (up to 3 levels deep)
    for _ in range(4):
        t = re.sub(r'\{\{[^{}]*\}\}', '', t)

    # Remove wiki tables {| ... |}
    t = re.sub(r'\{\|.*?\|\}', '', t, flags=re.DOTALL)

    # Remove File/Category/Image links
    t = re.sub(
        r'\[\[(?:File|Image|Category|चित्र|वर्गः|श्रेणी):[^\]]*\]\]',
        '', t, flags=re.IGNORECASE
    )

    # Convert wikilinks [[Target|Display]] → Display, [[Target]] → Target
    t = re.sub(r'\[\[(?:[^|\]]*\|)?([^\]]+)\]\]', r'\1', t)

    # Strip wiki-section headers (==Title==) — keep the title text
    t = re.sub(r'={2,}\s*(.*?)\s*={2,}', r'\1', t)

    # Remove bold/italic markup
    t = re.sub(r"'{2,}", '', t)

    # Remove horizontal rules
    t = re.sub(r'^-{4,}$', '', t, flags=re.MULTILINE)

    # Decode common HTML entities
    t = t.replace('&nbsp;', ' ').replace('&amp;', '&') \
         .replace('&lt;', '<').replace('&gt;', '>') \
         .replace('&quot;', '"').replace('&#160;', ' ')

    # Collapse runs of blank lines to at most one blank line
    t = re.sub(r'\n{3,}', '\n\n', t)

    return t.strip()


# ── UTF-16 LE writer ──────────────────────────────────────────────────────────

def save_utf16le(text: str, rel_path: str, dry_run: bool = False) -> None:
    abs_path = os.path.join(BASE_DIR, rel_path)
    if dry_run:
        print(f'    [dry-run] would save {len(text):,} chars → {rel_path}')
        return
    os.makedirs(os.path.dirname(abs_path), exist_ok=True)
    with open(abs_path, 'wb') as f:
        f.write(b'\xff\xfe')                    # UTF-16 LE BOM
        f.write(text.encode('utf-16-le'))
    print(f'    ✓ saved {len(text):,} chars → {rel_path}')


# ── Per-entry fetch logic ─────────────────────────────────────────────────────

def fetch_entry(entry: dict, dry_run: bool) -> bool:
    """Fetch, strip, and save one manifest entry. Returns True on success."""
    out_abs = os.path.join(BASE_DIR, entry['out'])
    if os.path.exists(out_abs):
        print(f"  SKIP  {entry['id']} — file already exists")
        return True

    # Collect page titles to fetch
    if 'pages' in entry:
        titles = entry['pages']
    elif 'page' in entry:
        titles = [entry['page']]
    elif 'prefix' in entry:
        print(f"  Discovering sub-pages for prefix '{entry['prefix']}' …")
        subpages = discover_subpages(entry['prefix'])
        if subpages:
            print(f"    found {len(subpages)} sub-page(s)")
            titles = subpages
        else:
            # No sub-pages found: try the bare prefix as a single page
            print(f"    no sub-pages found; trying as single page")
            titles = [entry['prefix']]
    else:
        print(f"  ERROR {entry['id']} — manifest entry has no page/prefix/pages key")
        return False

    # Checkpoint directory: cache each sub-page so interrupted runs can resume
    ckpt_dir = os.path.join(BASE_DIR, '.checkpoints', entry['id'])
    os.makedirs(ckpt_dir, exist_ok=True)

    # Fetch and strip each page (skip if already checkpointed)
    parts = []
    for idx, title in enumerate(titles):
        ckpt_file = os.path.join(ckpt_dir, f'{idx:04d}.txt')
        if os.path.exists(ckpt_file):
            with open(ckpt_file, 'r', encoding='utf-8') as cf:
                clean = cf.read()
            if clean:
                parts.append(clean)
            continue  # no API call needed

        print(f"    fetching '{title}' …", flush=True)
        raw = fetch_wikitext(title)
        time.sleep(SLEEP_SEC)
        if not raw:
            print(f"    WARNING: no content for '{title}'", flush=True)
            clean = ''
        else:
            clean = strip_markup(raw)
        # Always write checkpoint (even empty) so we don't re-fetch
        try:
            os.makedirs(ckpt_dir, exist_ok=True)  # re-create if somehow deleted
            with open(ckpt_file, 'w', encoding='utf-8') as cf:
                cf.write(clean)
        except OSError as ckpt_err:
            print(f"    WARNING: could not write checkpoint {ckpt_file}: {ckpt_err}", flush=True)
        if clean:
            parts.append(clean)

    if not parts:
        print(f"  ERROR {entry['id']} — no content retrieved")
        return False

    text = '\n\n'.join(parts)
    save_utf16le(text, entry['out'], dry_run)
    # Clean up checkpoints after successful save
    import shutil
    shutil.rmtree(ckpt_dir, ignore_errors=True)
    return True


# ── Ramayana kāṇḍa split ─────────────────────────────────────────────────────

KANDAS = [
    ('bAlakANDam',       'बालकाण्ड'),
    ('ayOdhyAkANDam',    'अयोध्याकाण्ड'),
    ('araNyakANDam',     'अरण्यकाण्ड'),
    ('kiShkiMdhAkANDam', 'किष्किन्धाकाण्ड'),
    ('suMdarakANDam',    'सुन्दरकाण्ड'),
    ('yuddhakANDam',     'युद्धकाण्ड'),
    ('uttarakANDam',     'उत्तरकाण्ड'),
]

def split_ramayana(dry_run: bool = False) -> None:
    src = os.path.join(BASE_DIR, 'rAmAyaNa', 'vAlmIki rAmAyaNa.txt')
    if not os.path.exists(src):
        print('ERROR: rAmAyaNa/vAlmIki rAmAyaNa.txt not found')
        return

    # Read UTF-16 LE
    with open(src, 'rb') as f:
        raw = f.read()
    if raw[:2] in (b'\xff\xfe', b'\xfe\xff'):
        text = raw.decode('utf-16')
    else:
        text = raw.decode('utf-16-le')

    lines = text.splitlines(keepends=True)
    print(f'Ramayana: {len(lines):,} lines total')

    # The file uses verse numbers "K.C.V text" where K=1..7 is the kāṇḍa.
    # Find the first line index for each kāṇḍa number prefix.
    verse_num_pat = re.compile(r'^(\d+)\.\d+\.\d+')
    split_points = []
    for kanda_num in range(1, 8):
        prefix = f'{kanda_num}.'
        for i, line in enumerate(lines):
            m = verse_num_pat.match(line.strip())
            if m and m.group(1) == str(kanda_num):
                split_points.append(i)
                break

    if len(split_points) != 7:
        print(f'WARNING: found {len(split_points)} kāṇḍa markers (expected 7).')
        print('Detected markers at lines:', split_points)
        print('Manual splitting required — check the source file.')
        return

    split_points.append(len(lines))  # sentinel for last kāṇḍa end

    for idx, (file_id, label) in enumerate(KANDAS):
        kanda_lines = lines[split_points[idx]:split_points[idx + 1]]
        kanda_text  = ''.join(kanda_lines).strip()
        out_rel     = f'rAmAyaNa/{file_id}.txt'
        out_abs     = os.path.join(BASE_DIR, out_rel)
        if os.path.exists(out_abs):
            print(f'  SKIP  {out_rel} — already exists')
            continue
        print(f'  {label}: {len(kanda_lines):,} lines')
        save_utf16le(kanda_text, out_rel, dry_run)


# ── CLI ───────────────────────────────────────────────────────────────────────

def main():
    args = sys.argv[1:]
    dry_run = '--dry-run' in args
    list_only = '--list' in args
    split_rama = '--split-ramayana' in args
    filters = [a for a in args if not a.startswith('--')]

    if list_only:
        print(f'Phase 1 manifest — {len(MANIFEST)} entries\n')
        for e in MANIFEST:
            out_abs = os.path.join(BASE_DIR, e['out'])
            status = '✓ exists' if os.path.exists(out_abs) else '✗ missing'
            print(f"  [{status}]  {e['id']:<30}  {e['label']}")
        print()
        print('Ramayana kāṇḍa split:')
        for fid, label in KANDAS:
            out_abs = os.path.join(BASE_DIR, 'rAmAyaNa', fid + '.txt')
            status = '✓ exists' if os.path.exists(out_abs) else '✗ missing'
            print(f"  [{status}]  {fid:<30}  {label}")
        return

    if split_rama:
        print('=== Splitting Valmiki Ramayana into kāṇḍas ===')
        split_ramayana(dry_run)
        return

    entries = MANIFEST
    if filters:
        def matches(e):
            return any(f.lower() in e['id'].lower() or f.lower() in e['label'] for f in filters)
        entries = [e for e in MANIFEST if matches(e)]
        if not entries:
            print(f'No entries match patterns: {filters}')
            return

    total   = len(entries)
    success = 0
    failed  = []

    for i, entry in enumerate(entries, 1):
        print(f'[{i}/{total}] {entry["id"]} — {entry["label"]}')
        try:
            ok = fetch_entry(entry, dry_run)
            if ok:
                success += 1
            else:
                failed.append(entry['id'])
        except Exception as exc:
            print(f'  ERROR: {exc}')
            failed.append(entry['id'])
        time.sleep(SLEEP_SEC)

    print(f'\n{"="*50}')
    print(f'Done: {success}/{total} succeeded')
    if failed:
        print(f'Failed: {", ".join(failed)}')
    print()
    print('Run with --split-ramayana to also split vAlmIki rAmAyaNa.txt into 7 kāṇḍas.')


if __name__ == '__main__':
    main()
