#!/usr/bin/env bash
# =============================================================================
# 1b_repackage_zips.sh
#
# Re-packages existing Lucene indexes into ZIPs WITHOUT re-indexing source
# texts.  Use this when only the ZIP contents changed — for example, after the
# fix that ensures .jaya-index-md.txt is included in each ZIP.
#
# Prerequisite: index_output/ must already exist (run 1_build_indexes.sh at
# least once beforehand).
#
# This is much faster than 1_build_indexes.sh because it skips the slow
# createMultipleIndexes() step and only runs createIndexZipFiles().
#
# Why the explicit copy step is needed
# ─────────────────────────────────────
# createIndexZipFiles() only copies a ZIP to jaya-index-files/v1/ when
# LastIndexMeta detects a change.  LastIndexMeta compares MD5 hashes of the
# *source text content* (files in to_be_indexed/), not the ZIP file itself.
# When only the ZIP packaging changes (e.g. a new file added to the ZIP) the
# source hashes are unchanged and LastIndexMeta skips the copy.  This script
# therefore copies every ZIP from index-zip-output-temp/ to jaya-index-files/v1/
# explicitly, bypassing that check.
#
# Usage:
#   ./scripts/1b_repackage_zips.sh              # repackage ZIPs then copy
#   ./scripts/1b_repackage_zips.sh --copy-only  # copy existing ZIPs, skip Gradle
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
INDEXER_DIR="$REPO_ROOT/UnicodeSearch/unicodesearch"
INDEX_OUTPUT="$INDEXER_DIR/index_output"
INDEX_ZIP_DIR="$INDEXER_DIR/index-zip-output-temp"
INDEX_FILES_V1="$REPO_ROOT/../jaya-index-files/v1"

COPY_ONLY=false
for arg in "$@"; do
  case "$arg" in
    --copy-only) COPY_ONLY=true ;;
    *)
      echo "Unknown argument: $arg"
      echo "Usage: $0 [--copy-only]"
      exit 1
      ;;
  esac
done

# ── Validate ────────────────────────────────────────────────────────────────

if [ ! -d "$INDEX_FILES_V1" ]; then
  echo "ERROR: jaya-index-files/v1/ not found: $INDEX_FILES_V1"
  echo "Clone https://github.com/openmuthu/jaya-index-files as a sibling of this repo."
  exit 1
fi

# ── Step 1: Regenerate ZIPs ─────────────────────────────────────────────────

if [ "$COPY_ONLY" = true ]; then
  echo "==> Skipping Gradle repackage (--copy-only)."
  if [ ! -d "$INDEX_ZIP_DIR" ] || [ -z "$(find "$INDEX_ZIP_DIR" -name "*.zip" 2>/dev/null)" ]; then
    echo "ERROR: index-zip-output-temp/ has no ZIPs to copy: $INDEX_ZIP_DIR"
    echo "Run without --copy-only first to generate the ZIPs."
    exit 1
  fi
else
  if [ ! -d "$INDEX_OUTPUT" ] || [ -z "$(ls -A "$INDEX_OUTPUT" 2>/dev/null)" ]; then
    echo "ERROR: index_output/ is missing or empty: $INDEX_OUTPUT"
    echo "Run ./scripts/1_build_indexes.sh first to generate the indexes."
    exit 1
  fi

  echo "==> Checking for Gradle..."
  if ! command -v gradle &>/dev/null; then
    echo ""
    echo "ERROR: 'gradle' not found in PATH."
    echo "Install Gradle (see README Environment Setup § 3) or run"
    echo "IndexerTest.repackageZips() directly from your IDE, then re-run"
    echo "this script with --copy-only."
    exit 1
  fi

  echo "==> Gradle version: $(gradle --version | grep '^Gradle' | head -1)"
  echo "==> Re-packaging ZIPs from existing indexes in: $INDEX_OUTPUT"
  echo ""

  cd "$INDEXER_DIR"

  echo "==> Running IndexerTest.repackageZips()..."
  gradle test --tests "org.jaya.scriptconverter.IndexerTest.repackageZips" \
    --rerun-tasks 2>&1 | tee /tmp/repackage_zips.log

  echo ""
  echo "==> Build log saved to /tmp/repackage_zips.log"

  if [ ! -d "$INDEX_ZIP_DIR" ]; then
    echo "ERROR: index-zip-output-temp/ was not created. Check the build log."
    exit 1
  fi
fi

ZIP_COUNT=$(find "$INDEX_ZIP_DIR" -name "*.zip" | wc -l | tr -d ' ')
echo ""
echo "==> $ZIP_COUNT ZIP(s) in index-zip-output-temp/:"
find "$INDEX_ZIP_DIR" -name "*.zip" -exec basename {} \; | sort

# ── Step 2: Explicit copy to jaya-index-files/v1/ ───────────────────────────
#
# LastIndexMeta only copies ZIPs whose *source text content* changed.
# Since only ZIP packaging changed here, all ZIPs must be copied manually.

echo ""
echo "==> Copying all ZIPs and catalogue files to jaya-index-files/v1/..."

COPIED=0
for zip in "$INDEX_ZIP_DIR"/*.zip; do
  [ -f "$zip" ] || continue
  name="$(basename "$zip")"
  dest="$INDEX_FILES_V1/$name"
  if [ -f "$dest" ] && cmp -s "$zip" "$dest"; then
    echo "  unchanged: $name"
  else
    cp "$zip" "$dest"
    echo "  copied:    $name"
    COPIED=$((COPIED + 1))
  fi
done

# Copy catalogue files too (always overwrite — they reflect current state)
for meta in catalogue.txt cat-details.txt; do
  src="$INDEX_ZIP_DIR/$meta"
  if [ -f "$src" ]; then
    cp "$src" "$INDEX_FILES_V1/$meta"
    echo "  copied:    $meta"
  fi
done

echo ""
echo "==> $COPIED ZIP(s) updated in jaya-index-files/v1/."
echo "==> Run ./scripts/2_publish_index.sh to push the changes to GitHub."
echo ""
echo "Done."
