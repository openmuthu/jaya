#!/usr/bin/env bash
# =============================================================================
# 0_run_tests.sh
#
# Runs all fast unit tests before the index build.  Must pass before
# 1_build_indexes.sh is allowed to proceed.
#
# Tests covered
# ─────────────
# Android (JVM unit tests — no device needed):
#   TOCTreeBuilderTest  — 22 tests for tree build / sort / flatten logic
#   ExampleUnitTest     — placeholder
#
# UnicodeSearch:
#   IndexerTest.zipFolder_metadataFileIncluded_otherDotFilesExcluded
#     — verifies .jaya-index-md.txt is present in generated ZIPs
#     — verifies other dot-files and the "app" folder remain excluded
#
# NOT covered here (require a connected device / emulator):
#   TableOfContentsActivityTest  (Espresso)
#   JayaIndexMetadataTest        (instrumented)
#
# Usage:
#   ./scripts/0_run_tests.sh
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID_DIR="$REPO_ROOT/android"
INDEXER_DIR="$REPO_ROOT/UnicodeSearch/unicodesearch"
FAILED=0

# ── Helpers ──────────────────────────────────────────────────────────────────

pass() { echo "  [PASS] $*"; }
fail() { echo "  [FAIL] $*"; FAILED=1; }
section() {
  echo ""
  echo "── $* ──────────────────────────────────────────────────"
}

# ── Android unit tests ───────────────────────────────────────────────────────

section "Android unit tests (android/)"

cd "$ANDROID_DIR"
if ./gradlew test 2>&1 | tee /tmp/android_unit_tests.log | grep -qE "BUILD SUCCESSFUL"; then
  pass "Android unit tests passed"
else
  fail "Android unit tests failed — see /tmp/android_unit_tests.log"
fi

# ── UnicodeSearch ZIP packaging test ────────────────────────────────────────

section "UnicodeSearch unit tests (UnicodeSearch/unicodesearch/)"

if ! command -v gradle &>/dev/null; then
  echo "  WARNING: 'gradle' not in PATH — skipping UnicodeSearch tests."
  echo "  Install Gradle to enable this check (see README Environment Setup)."
else
  cd "$INDEXER_DIR"
  if gradle test \
       --tests "org.jaya.scriptconverter.IndexerTest.zipFolder_metadataFileIncluded_otherDotFilesExcluded" \
       --rerun-tasks 2>&1 \
       | tee /tmp/unicodesearch_unit_tests.log \
       | grep -qE "BUILD SUCCESSFUL"; then
    pass "zipFolder_metadataFileIncluded_otherDotFilesExcluded passed"
  else
    fail "zipFolder test failed — see /tmp/unicodesearch_unit_tests.log"
  fi
fi

# ── Result ───────────────────────────────────────────────────────────────────

echo ""
if [ "$FAILED" -ne 0 ]; then
  echo "RESULT: One or more test suites FAILED. Fix failures before building indexes."
  exit 1
else
  echo "RESULT: All tests passed."
fi
