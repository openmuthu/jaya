#!/usr/bin/env bash
# =============================================================================
# 1_build_indexes.sh
#
# Builds Lucene indexes for all content categories and packages them as ZIPs.
# Runs IndexerTest.runIndexer() inside UnicodeSearch/unicodesearch.
#
# Outputs:
#   index_output/<category>/        — Lucene index per category
#   index-zip-output-temp/*.zip     — ZIPs ready for publishing
#   ../jaya-index-files/v1/         — ZIPs + catalogue files copied here
#
# Usage:
#   ./scripts/1_build_indexes.sh
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
INDEXER_DIR="$REPO_ROOT/UnicodeSearch/unicodesearch"
SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"

SKIP_TESTS=false
for arg in "$@"; do
  case "$arg" in
    --skip-tests) SKIP_TESTS=true ;;
  esac
done

# ── Run unit tests first ─────────────────────────────────────────────────────

if [ "$SKIP_TESTS" = true ]; then
  echo "==> Skipping unit tests (--skip-tests)."
else
  echo "==> Running unit tests before build..."
  bash "$SCRIPTS_DIR/0_run_tests.sh"
  echo ""
fi

echo "==> Checking for Gradle..."
if ! command -v gradle &>/dev/null; then
  echo ""
  echo "ERROR: 'gradle' not found in PATH."
  echo "Install Gradle (https://gradle.org/install/) or run the test from"
  echo "your IDE: open UnicodeSearch/unicodesearch as a Gradle project and"
  echo "run IndexerTest.runIndexer()."
  exit 1
fi

echo "==> Gradle version: $(gradle --version | grep '^Gradle' | head -1)"
echo "==> Indexer directory: $INDEXER_DIR"
echo ""

cd "$INDEXER_DIR"

echo "==> Running IndexerTest.runIndexer()..."
echo "    This re-indexes all categories and may take several minutes."
echo "    (Existing index_output/ directories are deleted per-category"
echo "     before each build to prevent duplicate Lucene segments.)"
echo ""

gradle test --tests "org.jaya.scriptconverter.IndexerTest.runIndexer" \
  --rerun-tasks \
  --info 2>&1 | tee /tmp/indexer_build.log

echo ""
echo "==> Build log saved to /tmp/indexer_build.log"

INDEX_ZIP_DIR="$INDEXER_DIR/index-zip-output-temp"
if [ -d "$INDEX_ZIP_DIR" ]; then
  ZIP_COUNT=$(find "$INDEX_ZIP_DIR" -name "*.zip" | wc -l | tr -d ' ')
  echo ""
  echo "==> $ZIP_COUNT ZIP(s) generated in index-zip-output-temp/:"
  find "$INDEX_ZIP_DIR" -name "*.zip" -exec basename {} \; | sort
fi

INDEX_FILES_V1="$REPO_ROOT/../jaya-index-files/v1"
if [ -d "$INDEX_FILES_V1" ]; then
  echo ""
  echo "==> jaya-index-files/v1/ updated. Run 2_publish_index.sh to push to GitHub."
else
  echo ""
  echo "WARNING: $INDEX_FILES_V1 not found."
  echo "  Clone https://github.com/openmuthu/jaya-index-files as a sibling of this repo."
fi

echo ""
echo "Done."
