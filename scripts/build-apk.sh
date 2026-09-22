#!/system/bin/sh
set -e
cd "$(dirname "$0")/.."
echo "=== حارس يونس: بناء APK ==="
if command -v gradle >/dev/null 2>&1; then
  gradle --no-daemon assembleDebug
else
  echo "لم يتم العثور على Gradle. افتح المشروع في AndroidIDE وثبّت أدوات البناء المطلوبة أولًا."
  exit 1
fi
echo "=== تم البناء ==="
echo "APK: app/build/outputs/apk/debug/app-debug.apk"
