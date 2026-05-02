# Release Notes

Each version entry includes the git range it covers so future release notes can be
generated with:
```
git log <range-start>..<range-end> --oneline
```

---

## Version 12.0 — 2026-05-02

**Git range:** `0154a58..7978be1`
Key commits: `3f3cc38` (TOC tree), `41a7285` (TOC search), `b3d8072` (Verse Navigator), `7978be1` (digit normalisation)

### Search
- **Folder-scoped search from Table of Contents** — each item in the Table of Contents now shows an inline search icon. Tapping it opens a search scoped to that folder or text, so results are limited to the selected grantha rather than the entire library.

### Navigation
- **Verse Navigator** — when viewing a text that contains verse numbers (formats: `x.y`, `x.y.z`, `x-y`, etc.), a "Go to verse" option appears in the action bar. Tapping it opens a slide-up sheet showing a collapsible chapter → verse tree. Tapping any verse jumps directly to it.
  - Verse previews show the first ~30 characters of each verse's content, stripped of whitespace and special characters, displayed in your chosen script (ITRANS / Devanagari / Kannada / Telugu).

### Table of Contents
- **Tree view** — the Table of Contents now renders as a collapsible tree, making it easier to browse nested folder structures within the library.

### Content Updates
- **New Āgama texts added** — several additional Āgama texts are now available for download.
- **Ashtadhyayi (Pāṇini)** — sutra numbering corrected: sections are now in the canonical order (1.1 → 8.4) and all verse numbers use standard ASCII digits for consistent search and verse navigation.
- **Digit normalisation** — verse and section numbers across 22 additional texts (Hayaśīrṣa Pañcarātra, Nyāyasudhā volumes, Vājasaneyī Saṃhitā, Gautama Smṛti, and others) have been converted from Devanagari numerals to ASCII for consistent indexing and verse navigation.

---

## Version 11.0 — 2025-05-23

**Git range:** `5aa04cd..0154a58`
Key commits: `0154a58`

- Fixed views being obscured by the system action bar on Android 15 (API 35).

---

## Version 10.0 — 2025-05-20

**Git range:** `d66b720..5aa04cd`
Key commits: `5aa04cd`

- Raised `targetSdkVersion` to 35 to comply with Google Play requirements.

---

## Version 7.0 — 2024-04-08

**Git range:** `b139d77..d66b720`
Key commits: `c37f9e7`, `d66b720`

- Raised `targetSdkVersion` to 33.
- Added new texts:
  - tatvasuvvAli, pramANapaddhati TIkA, yOgadIpikA, sadAchArasmRti TIkA
  - dvAdasha stOtra TIkA, vAyustuti TIkA, yamakabhArata yAdupatya TIkA
  - karma nirNaya TIkA, tatvavivEka TIkA, tatvasaMkhyAna TIkA
  - tatvOdyOta TIkA, tatva nirNaya TIkA, aitarEya tAmraparNi TIkA
  - bRhadAraNyaka bhAvabOdha, vAdAvalI saTIkA
  - Purāṇas: dEvI bhAgavataM, gArga saMhitA, kapila purāṇa, nandi purāṇa, nIlamata purāṇa, parāshara purāṇa
  - mEghadUtaM TIkA, raghuvaMsha TIkA

---

## Version 6.0 — 2021-02-01

**Git range:** `8e34654..b139d77`
Key commits: `1e52590`, `0cf3eb8`, `b139d77`

- Upgraded project to Android Studio 4.1.2.
- Added preference to configure the number of items copied when using the "Copy" option.

---

## Version 5.0 — 2021-01-10

**Git range:** `32be98c..8e34654`
Key commits: `8e34654`

- Fixed multiple crashes caused by index files not being created or storage permissions not being available. The app now directs users to the Settings page to download content rather than crashing.

---

## Version 4.0 — 2021-01-02

**Git range:** `86783b3..32be98c`
Key commits: `32be98c`

- Fixed storage access on Android 10 and above: the app now uses app-specific storage instead of external shared storage, which is no longer permitted.

---

## Version 2.0 — 2020-12-29

**Git range:** `fb7a8df..86783b3`
Key commits: `d1e2f66` (TOC), `67389be` (multi-script search), `607bef6` (browse after search), `7ef6824` (bug fixes), `ce3a54f` (new content)

- **Table of Contents** — added a Table of Contents activity for browsing the library structure.
- **Multi-script search** — search strings can now be entered in any supported script (ITRANS, Devanagari, Kannada, Telugu); the app converts automatically before querying the index.
- **Browse after search** — users can now browse content directly from search results.
- **Search results with highlights** — matching terms are highlighted in search results.
- **Privacy policy** added.
- **New content** — added Nyāyasudhā samagra, Advaita texts, Gītā Vivṛti, Prameya Dīpikā, Mahābhārata Mādhva Pāṭha, Lakṣālaṃkāra, kṛṣṇacārityamañjarī, nadītāratamyastōtram, rāghavēndragāthāstava, svāpnavṛndāvanākhyāna, tīrthaprabaṃdha, new ShaTprashna texts, mahānārāyaṇa upaniṣat, additional Stōtras, and dāsasāhitya files.
- Various crash fixes and stability improvements.

---

## Version 1.0 — 2017-02-20

**Git range:** initial commit — `fb7a8df`

- Initial release with Lucene-based Unicode/ITRANS search.
- Support for Devanagari, Kannada, and Telugu scripts via the ScriptConverter.
- Android app with search integrated against indexed Sanskrit texts.
