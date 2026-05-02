#!/usr/bin/env bash
# =============================================================================
# deploy_all.sh
#
# Master script: runs all three steps in sequence.
#
#   1. Build Lucene indexes + package ZIPs  (IndexerTest.runIndexer)
#   2. Publish to jaya-index-files on GitHub
#   3. Build the Android debug APK
#
# Usage:
#   ./scripts/deploy_all.sh
#   ./scripts/deploy_all.sh --skip-index   # skip step 1 (indexes already built)
#   ./scripts/deploy_all.sh --skip-publish # skip step 2 (don't push to GitHub)
#   ./scripts/deploy_all.sh --install      # install APK on connected device after build
#   ./scripts/deploy_all.sh "commit message for jaya-index-files"
# =============================================================================

set -euo pipefail

SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"

SKIP_TESTS=false
SKIP_INDEX=false
SKIP_PUBLISH=false
INSTALL_FLAG=""
COMMIT_MSG=""

for arg in "$@"; do
  case "$arg" in
    --skip-tests)   SKIP_TESTS=true ;;
    --skip-index)   SKIP_INDEX=true; SKIP_TESTS=true ;;
    --skip-publish) SKIP_PUBLISH=true ;;
    --install)      INSTALL_FLAG="--install" ;;
    --*)
      echo "Unknown flag: $arg"
      echo "Usage: $0 [--skip-tests] [--skip-index] [--skip-publish] [--install] [\"commit message\"]"
      exit 1
      ;;
    *) COMMIT_MSG="$arg" ;;
  esac
done

echo "======================================================================"
echo "  Jaya — Full Deploy Pipeline"
echo "======================================================================"
echo ""

# ── Step 1: Unit tests ──────────────────────────────────────────────────────

if [ "$SKIP_TESTS" = true ]; then
  echo "[Step 1] Skipping unit tests (--skip-tests)."
else
  echo "[Step 1/4] Running unit tests..."
  echo "----------------------------------------------------------------------"
  bash "$SCRIPTS_DIR/0_run_tests.sh"
fi
echo ""

# ── Step 2: Build indexes ───────────────────────────────────────────────────

if [ "$SKIP_INDEX" = true ]; then
  echo "[Step 2] Skipping index build (--skip-index)."
else
  echo "[Step 2/4] Building indexes and ZIPs..."
  echo "----------------------------------------------------------------------"
  bash "$SCRIPTS_DIR/1_build_indexes.sh" --skip-tests
fi
echo ""

# ── Step 3: Publish ─────────────────────────────────────────────────────────

if [ "$SKIP_PUBLISH" = true ]; then
  echo "[Step 3] Skipping publish (--skip-publish)."
else
  echo "[Step 3/4] Publishing index files to GitHub..."
  echo "----------------------------------------------------------------------"
  if [ -n "$COMMIT_MSG" ]; then
    bash "$SCRIPTS_DIR/2_publish_index.sh" "$COMMIT_MSG"
  else
    bash "$SCRIPTS_DIR/2_publish_index.sh"
  fi
fi
echo ""

# ── Step 4: Build APK ───────────────────────────────────────────────────────

echo "[Step 4/4] Building Android APK..."
echo "----------------------------------------------------------------------"
bash "$SCRIPTS_DIR/3_build_apk.sh" $INSTALL_FLAG

echo ""
echo "======================================================================"
echo "  All steps complete."
echo "======================================================================"
