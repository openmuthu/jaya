#!/usr/bin/env bash
# =============================================================================
# 2_publish_index.sh
#
# Commits and pushes updated index ZIPs and catalogue files in jaya-index-files.
#
# Requires:
#   - jaya-index-files repo cloned as a sibling of this repo
#   - 1_build_indexes.sh has been run first
#
# Usage:
#   ./scripts/2_publish_index.sh
#   ./scripts/2_publish_index.sh "Added new texts: tatvasuvvAli, pramANapaddhati"
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
INDEX_FILES_REPO="$REPO_ROOT/../jaya-index-files"
INDEX_FILES_V1="$INDEX_FILES_REPO/v1"

# ── Validate ────────────────────────────────────────────────────────────────

if [ ! -d "$INDEX_FILES_REPO" ]; then
  echo "ERROR: jaya-index-files repo not found at: $INDEX_FILES_REPO"
  echo "Clone it: git clone https://github.com/openmuthu/jaya-index-files"
  echo "It must be a sibling directory of this repo."
  exit 1
fi

if [ ! -d "$INDEX_FILES_V1" ]; then
  echo "ERROR: v1/ folder not found inside jaya-index-files."
  exit 1
fi

cd "$INDEX_FILES_REPO"

# ── Check for changes ───────────────────────────────────────────────────────

CHANGED=$(git status --porcelain v1/ | wc -l | tr -d ' ')
if [ "$CHANGED" -eq 0 ]; then
  echo "No changes in jaya-index-files/v1/ — nothing to publish."
  exit 0
fi

echo "==> Changed files in v1/:"
git status --short v1/
echo ""

# ── Commit message ──────────────────────────────────────────────────────────

if [ $# -ge 1 ] && [ -n "$1" ]; then
  COMMIT_MSG="$1"
else
  # Auto-generate from changed ZIPs
  CHANGED_ZIPS=$(git status --porcelain v1/*.zip 2>/dev/null \
    | awk '{print $2}' | xargs -I{} basename {} .zip | sort | tr '\n' ', ' | sed 's/,$//')
  if [ -n "$CHANGED_ZIPS" ]; then
    COMMIT_MSG="Update index: $CHANGED_ZIPS"
  else
    COMMIT_MSG="Update index catalogue"
  fi
fi

echo "==> Commit message: $COMMIT_MSG"
echo ""

# ── Stage, commit, push ─────────────────────────────────────────────────────

git add v1/

echo "==> Committing..."
git commit -m "$COMMIT_MSG"

echo ""
echo "==> Pushing to origin/master..."
git push origin master

echo ""
echo "Done. Changes are live at:"
echo "  https://raw.githubusercontent.com/openmuthu/jaya-index-files/master/v1/"
echo ""
echo "Users must re-download affected categories in the app for changes to take effect."
