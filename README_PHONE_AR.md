# حارس يونس — بناء APK من الجوال

هذه النسخة مجهزة للبناء من هاتف Android باستخدام بيئة تطوير Android تدعم Gradle.

## الطريقة المقترحة: AndroidIDE

> ملاحظة: مشروع AndroidIDE الأصلي مؤرشف وغير مُصان حاليًا؛ استخدم مصدرًا موثوقًا فقط، وتحقق من توافق جهازك وإصدار AndroidIDE قبل تثبيته.

1. نزّل وثبّت AndroidIDE من مصدر موثوق.
2. افتح AndroidIDE.
3. اختر **Open Project**.
4. فك ضغط `Haras_Younis_v11_phone.zip` في مجلد يسهل الوصول إليه.
5. افتح مجلد `Haras_Younis_v11_phone` كمشروع Gradle.
6. اسمح لـ Gradle بمزامنة المشروع.
7. ثبّت Android SDK/Build Tools المطلوبة إذا طلبها البرنامج.
8. من Terminal داخل AndroidIDE شغّل:

```sh
cd /path/to/Haras_Younis_v11_phone
gradle assembleDebug
```

أو شغّل:

```sh
./scripts/build-apk.sh
```

بعد نجاح البناء ستجد:

`app/build/outputs/apk/debug/app-debug.apk`

يمكن فتح ملف APK من مدير الملفات وتثبيته على الهاتف.

## ملاحظات مهمة

- هذه نسخة Debug قابلة للتثبيت للاختبار.
- لا تحتاج إلى كمبيوتر.
- عند أول تشغيل للحماية سيطلب Android إذن إنشاء اتصال VPN؛ وافق عليه.
- يجب اختبار حجب Wi‑Fi وحجب بيانات الجوال فعليًا على الجهاز، لأن سلوك VPN يختلف حسب إصدار Android والشركة المصنعة.
- للحصول على نسخة Release موقعة للنشر، يلزم إنشاء مفتاح توقيع خاص بك.
