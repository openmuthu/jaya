# AgamAnvEShiNI — Build & Deploy Guide

AgamAnvEShiNI (आगमान्वेषिणी) is an Android search app for Sanskrit texts. Content is
pre-indexed on a developer machine, packaged as per-category ZIP files, published to
GitHub, and downloaded on demand by the app.

---

## Repository layout

```
github/
├── jaya/                          ← this repo (app + indexer)
│   ├── android/                   ← Android app (Gradle)
│   └── UnicodeSearch/
│       └── unicodesearch/
│           ├── to_be_indexed/     ← source texts (one subfolder per category)
│           ├── index_output/      ← generated Lucene indexes (git-ignored)
│           └── index-zip-output-temp/  ← generated ZIPs (git-ignored)
└── jaya-index-files/              ← sibling repo — published index ZIPs
    └── v1/
        ├── catalogue.txt
        ├── cat-details.txt
        └── *.zip
```

> `jaya` and `jaya-index-files` **must be cloned into the same parent directory**
> (e.g. `~/Documents/github/`). Path constants in `Constatants.java` are derived
> automatically at runtime by walking up from the compiled `.class` file location.

---

## Environment setup

### 1. Clone both repos side by side

Both repos **must share the same parent directory**. Path constants in
`Constatants.java` are derived at runtime by walking up from the compiled
`.class` location — no configuration file is needed, but the layout must match.

```bash
cd ~/Documents/github          # or any directory you prefer
git clone https://github.com/openmuthu/jaya.git
git clone https://github.com/openmuthu/jaya-index-files.git
```

### 2. Java 8+

```bash
# macOS
brew install openjdk@11        # or @8, @17 — anything ≥ 8
# Verify
java -version
```

### 3. Gradle (for UnicodeSearch indexer only)

The Android sub-project ships with its own `gradlew` wrapper and needs nothing
extra. The `UnicodeSearch/unicodesearch` project does **not** have a wrapper, so
a system Gradle installation is required to run `0_run_tests.sh` and
`1_build_indexes.sh`.

```bash
# macOS
brew install gradle
# Verify
gradle --version               # must be 7.x or later
```

If Gradle is not installed, `0_run_tests.sh` will skip the UnicodeSearch tests
with a warning, and `1_build_indexes.sh` will exit with an error.

### 4. Android Studio and SDK

Download Android Studio from https://developer.android.com/studio.

After installation, open `android/` as a Gradle project and let Android Studio
download the required SDK components automatically. The project targets:

| Setting | Value |
|---------|-------|
| compileSdkVersion | 35 |
| targetSdkVersion | 35 |
| minSdkVersion | 14 (Android 4.0) |

`adb` (used by `3_build_apk.sh --install`) is in the Android SDK
`platform-tools` folder. Add it to your PATH:

```bash
# Add to ~/.zshrc or ~/.bash_profile
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"
```

### 5. Release signing (optional — only needed for `--release` builds)

The release signing config is intentionally not committed. To build a signed
release APK, set these environment variables before running `3_build_apk.sh`:

```bash
export JAYA_KEYSTORE="/path/to/jaya.p12"   # path to your keystore file
export JAYA_KEY_ALIAS="key0"               # key alias inside the keystore
export JAYA_STORE_PASS="<keystore-password>"
export JAYA_KEY_PASS="<key-password>"
```

Then:

```bash
./scripts/3_build_apk.sh --release
```

Add those `export` lines to your shell profile (`~/.zshrc` / `~/.bash_profile`)
to avoid re-entering them each session. The keystore file itself should be kept
outside the repo directory and never committed.

---

## Step 1 — Add or update source texts

Place `.txt` files (ITRANS-encoded) inside the appropriate category folder under
`UnicodeSearch/unicodesearch/to_be_indexed/`. Each top-level subfolder becomes one
downloadable category in the app.

Current categories: `AgamAH`, `advaitam`, `dAsasAhitya`, `harikathAmRtasAra`,
`itaragraMthAH`, `kOsha`, `mAdhva`, `mImAMsA`, `madhvavijaya`, `mahAbhArata`,
`mahAbhArata_mAdhvapATha`, `nirukti`, `purANa`, `rAmAyaNa`, `sAhitya`, `smRti`,
`stOtra`, `strIdharma`, `vEda`, `vyAkaraNa`.

---

## Automation scripts

The steps below are scripted. Unit tests gate the index build — the build will
not run if tests fail.

```bash
./scripts/0_run_tests.sh                           # run unit tests only
./scripts/1_build_indexes.sh                       # tests → build indexes + ZIPs
./scripts/1b_repackage_zips.sh                     # re-ZIP + copy to jaya-index-files
./scripts/1b_repackage_zips.sh --copy-only         # copy existing ZIPs, skip Gradle
./scripts/2_publish_index.sh "what changed"        # commit + push to jaya-index-files
./scripts/3_build_apk.sh [--release] [--install]   # build Android APK

# Or the full pipeline at once:
./scripts/deploy_all.sh "what changed"
./scripts/deploy_all.sh --skip-tests   # skip tests (already passing)
./scripts/deploy_all.sh --skip-index   # ZIPs already built, just publish + APK
./scripts/deploy_all.sh --install      # also install APK on connected device after build
```

**When to use `1b_repackage_zips.sh` vs `1_build_indexes.sh`:**

| Scenario | Script |
|----------|--------|
| Added or updated source texts in `to_be_indexed/` | `1_build_indexes.sh` — must re-index |
| Only ZIP packaging changed (e.g. the `.jaya-index-md.txt` fix) | `1b_repackage_zips.sh` — skip re-indexing, just re-ZIP |
| First time setup / `index_output/` missing | `1_build_indexes.sh` |

`2_publish_index.sh` auto-generates a commit message from the changed ZIP names if
none is provided. `3_build_apk.sh --install` calls `adb install -r` and requires
`adb` in PATH (see Environment Setup § 4).

**What the tests cover:**

| Test class | Where | What |
|---|---|---|
| `TOCTreeBuilderTest` | `android/` (JVM) | 22 tests: tree build, sort, flatten, leading-slash handling |
| `IndexerTest.zipFolder_…` | `UnicodeSearch/` (JVM) | `.jaya-index-md.txt` included in ZIPs; other dot-files excluded |
| `TableOfContentsActivityTest` | Android instrumented | Espresso UI tests — require a connected device; run manually |
| `JayaIndexMetadataTest` | Android instrumented | Metadata persistence — require a connected device; run manually |

---

## Step 2 — Build the indexes and ZIP files

The indexer is a JUnit test in the `UnicodeSearch/unicodesearch` Gradle project.
It does two things in sequence:

1. **`createMultipleIndexes`** — reads each category folder in `to_be_indexed/`,
   builds a Lucene index, and writes it to `index_output/<category>/`.
2. **`createIndexZipFiles`** — zips each `index_output/<category>/` folder (including
   `.jaya-index-md.txt`) into `index-zip-output-temp/<category>.zip`, generates
   `catalogue.txt` and `cat-details.txt`, and copies updated files to
   `../jaya-index-files/v1/`.

### Run from the command line

```bash
cd UnicodeSearch/unicodesearch
./gradlew test --tests "org.jaya.scriptconverter.IndexerTest.runIndexer"
```

### Run from an IDE

Open `UnicodeSearch/unicodesearch` as a Gradle project, then run the
`IndexerTest.runIndexer` JUnit test. The test simply calls `main()` which
invokes `createMultipleIndexes` followed by `createIndexZipFiles`.

### What gets generated

After a successful run:

```
index-zip-output-temp/
├── AgamAH.zip           ← Lucene index + .jaya-index-md.txt for this category
├── purANa.zip
├── vEda.zip
└── ...

../jaya-index-files/v1/
├── catalogue.txt        ← index of all categories (JSON, read by the app on startup)
├── cat-details.txt      ← per-category file lists (JSON)
├── AgamAH.zip           ← same ZIPs copied here for publishing
├── purANa.zip
└── ...
```

> **Important:** `index-zip-output-temp/` contains only the ZIPs whose content has
> changed since the last run. The full set lives in `jaya-index-files/v1/`.

---

## Step 3 — Publish the index files to GitHub

```bash
cd ../jaya-index-files

git add v1/
git commit -m "Update index: <describe what changed>"
git push origin master
```

The app fetches all index data from:

```
https://raw.githubusercontent.com/openmuthu/jaya-index-files/master/v1/
```

Changes are live as soon as the push completes (GitHub CDN may add a short delay).

---

## Step 4 — Build the Android app

```bash
cd jaya/android

# Debug build
./gradlew assembleDebug

# Release build (requires signing config in local.properties or build.gradle)
./gradlew assembleRelease
```

Or open `android/` in Android Studio and use **Build → Build Bundle(s) / APK(s)**.

Key app identifiers:

| Field | Value |
|-------|-------|
| Application ID | `org.jaya` |
| Min SDK | 14 (Android 4.0) |
| Target SDK | 35 (Android 15) |
| Version code | 11 |
| Version name | 11.0 |

---

## Step 5 — Install and verify on device

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

To verify a newly published category appears in the Table of Contents:

1. Open the app → **Download** screen.
2. Download the updated category (e.g. AgamAH).
3. Open **Table of Contents** — the category and its texts should appear.

> If a category was downloaded **before** the `.jaya-index-md.txt` fix (commit
> that changed `IndexerTest.zipFolder` to include the metadata file), delete it
> from the app and re-download. Already-installed categories do not retroactively
> update their metadata. No app update is needed — only a re-download.

---

## How the install pipeline works (end-to-end)

```
Developer machine                    GitHub CDN                   Android device
─────────────────                    ──────────                   ──────────────
to_be_indexed/AgamAH/                                             
  └─ *.txt                           
       │                             
  IndexerTest.runIndexer()           
       │                             
  index_output/AgamAH/               
  ├── Lucene segment files           
  └── .jaya-index-md.txt  ──zip──►  jaya-index-files/v1/
                                     AgamAH.zip          ──download──►  temp/AgamAH/
                                     catalogue.txt       ──fetch──►          │
                                                                    mergeIndexes()
                                                                         │
                                                                    device Lucene index
                                                                    + .jaya-index-md.txt
                                                                         │
                                                                    TableOfContents
                                                                    shows AgamAH ✓
```

`mergeIndexes()` in `LuceneUnicodeFileIndexer`:
- Reads `.jaya-index-md.txt` from the unzipped temp folder.
- Merges the Lucene index segments into the app's main index.
- Appends the file paths to the device's own `.jaya-index-md.txt` (the Table of
  Contents data source).

---

## Troubleshooting

**Category not appearing in Table of Contents after download**
- Ensure you are running `IndexerTest.runIndexer()` after the fix that includes
  `.jaya-index-md.txt` in the ZIP.
- Delete the category in the app and re-download.

**`JAYA_COMMON_HOME` resolves to the wrong directory**
- Both `jaya` and `jaya-index-files` must be siblings in the same directory.
  The path is derived by walking up 10 levels from the compiled
  `Constatants.class` file. If the project structure changes, update
  `deriveJayaCommonHome()` in `Constatants.java`.

**IndexerTest takes a long time**
- Expected — it re-indexes every text file in all categories. Run only
  `createIndexZipFiles` (comment out `createMultipleIndexes` in `main()`) when
  only the packaging step needs to be re-run.
