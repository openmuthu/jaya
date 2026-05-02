#!/usr/bin/env bash
# =============================================================================
# 3_build_apk.sh
#
# Builds the Android APK (debug by default, release with --release flag).
#
# Usage:
#   ./scripts/3_build_apk.sh              # debug build
#   ./scripts/3_build_apk.sh --release    # release build (requires signing config)
#   ./scripts/3_build_apk.sh --install    # debug build + install via adb
#
# Release signing:
#   Uncomment and fill in the signingConfigs block in android/app/build.gradle,
#   or set these environment variables:
#     JAYA_KEYSTORE   — path to the .p12 / .jks keystore file
#     JAYA_KEY_ALIAS  — key alias inside the keystore
#     JAYA_STORE_PASS — keystore password
#     JAYA_KEY_PASS   — key password
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID_DIR="$REPO_ROOT/android"

BUILD_TYPE="debug"
INSTALL=false

for arg in "$@"; do
  case "$arg" in
    --release) BUILD_TYPE="release" ;;
    --install) INSTALL=true ;;
    *)
      echo "Unknown argument: $arg"
      echo "Usage: $0 [--release] [--install]"
      exit 1
      ;;
  esac
done

# ── Signing check for release ───────────────────────────────────────────────

if [ "$BUILD_TYPE" = "release" ]; then
  if [ -z "${JAYA_KEYSTORE:-}" ]; then
    echo "ERROR: Release build requires signing configuration."
    echo ""
    echo "Either:"
    echo "  1. Set environment variables:"
    echo "       export JAYA_KEYSTORE=/path/to/jaya.p12"
    echo "       export JAYA_KEY_ALIAS=key0"
    echo "       export JAYA_STORE_PASS=<password>"
    echo "       export JAYA_KEY_PASS=<password>"
    echo "  2. Uncomment the signingConfigs block in android/app/build.gradle"
    exit 1
  fi
fi

# ── Build ───────────────────────────────────────────────────────────────────

cd "$ANDROID_DIR"

echo "==> Build type: $BUILD_TYPE"
echo "==> Android project: $ANDROID_DIR"
echo ""

GRADLE_TASK="assemble$(tr '[:lower:]' '[:upper:]' <<< "${BUILD_TYPE:0:1}")${BUILD_TYPE:1}"

GRADLE_PROPS=()
if [ "$BUILD_TYPE" = "release" ] && [ -n "${JAYA_KEYSTORE:-}" ]; then
  GRADLE_PROPS+=(
    "-PstoreFile=${JAYA_KEYSTORE}"
    "-PkeyAlias=${JAYA_KEY_ALIAS:-key0}"
    "-PstorePassword=${JAYA_STORE_PASS}"
    "-PkeyPassword=${JAYA_KEY_PASS}"
  )
fi

echo "==> Running: ./gradlew $GRADLE_TASK"
./gradlew "$GRADLE_TASK" "${GRADLE_PROPS[@]+"${GRADLE_PROPS[@]}"}"

# ── Report APK location ─────────────────────────────────────────────────────

APK_DIR="$ANDROID_DIR/app/build/outputs/apk/$BUILD_TYPE"
APK_FILE=$(find "$APK_DIR" -name "*.apk" 2>/dev/null | head -1)

if [ -z "$APK_FILE" ]; then
  echo "WARNING: APK not found in expected location: $APK_DIR"
else
  APK_SIZE=$(du -sh "$APK_FILE" | cut -f1)
  echo ""
  echo "==> APK built: $APK_FILE ($APK_SIZE)"
fi

# ── Optional: install on connected device ───────────────────────────────────

if [ "$INSTALL" = true ]; then
  if ! command -v adb &>/dev/null; then
    echo ""
    echo "WARNING: 'adb' not found in PATH — skipping install."
    echo "Add Android SDK platform-tools to your PATH."
  elif [ -z "$APK_FILE" ]; then
    echo "WARNING: No APK found to install."
  else
    DEVICES=$(adb devices | grep -v "^List" | grep "device$" | wc -l | tr -d ' ')
    if [ "$DEVICES" -eq 0 ]; then
      echo ""
      echo "WARNING: No Android device connected — skipping install."
    else
      echo ""
      echo "==> Installing on device..."
      adb install -r "$APK_FILE"
      echo "==> Installed successfully."
    fi
  fi
fi

echo ""
echo "Done."
