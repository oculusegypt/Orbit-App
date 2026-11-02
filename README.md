<div dir="rtl">

# 🪐 ORBIT Learn 2026 — مدار للتعلم الرقمي

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green.svg)](https://developer.android.com/jetpack/compose)
[![Gemini](https://img.shields.io/badge/Gemini%202.0%20Flash-Socratic%20Tutor-9b7aed.svg)](https://ai.google.dev)
[![Room](https://img.shields.io/badge/Room-SQLite-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Hilt](https://img.shields.io/badge/Hilt-DI-4a90d9.svg)](https://dagger.dev/hilt/)
[![Min SDK](https://img.shields.io/badge/minSdk-24-lightgrey.svg)](#)

> **منصة تعليمية رقمية عربية متكاملة** — فصول حية، مساعد ذكي سقراطي، مكالمات خاصة، شهادات موثّقة، وألعاب حساب ذهني.
> **100% ميزات حقيقية فعّالة — صفر محاكاة (No Mocks Doctrine).**

📌 للمطورين: راجع **[الذاكرة المعمارية العميقة للمشروع](.memory/PROJECT_MEMORY.md)** — المرجع الهندسي الكامل للطبقات وعقود التكامل.

---

## ✨ لماذا مدار؟

| | الميزة | كيف تعمل فعلياً |
|---|---|---|
| 🤖 | **مساعد سارة السقراطي** | استدعاءات حقيقية لـ Gemini 2.0 Flash مع system instruction يمنع إعطاء الحل مباشرة، وسياق متدفق من آخر 10 رسائل محفوظة في Room |
| 🎥 | **فصول افتراضية حية** | WebRTC فعلي عبر Jitsi Meet داخل WebView مع أذونات كاميرا/ميكروفون حقيقية، غرف breakout، ومراقب صوت حي |
| 📞 | **اتصال خاص 1-1** | signaling حقيقي عبر FCM topics (`user_<id>`) — الدعوة تصل للطرف الآخر على أي جهاز، وقبول/رفض يسجَّل بدورته الكاملة في قاعدة البيانات |
| 🎓 | **شهادات معتمدة قابلة للتحقق** | تُصدر عند إكمال 100%، تُحفظ دائماً في Room، وتُتحقق برمز ثنائي الطبقات (كتالوج + قاعدة بيانات) |
| 💳 | **دفع وكوبونات حقيقية** | محرك كوبونات `OrbitCouponEngine`، إيصالات شراء دائمة، وفتح محتوى الدورة بعد الشراء |
| 🧮 | **لعبة الحساب الذهني** | توليد عشوائي حتمي، XP وStreak تتراكم ذرياً في Room ولا تضيع عند إغلاق التطبيق |
| 📚 | **مشغّل دروس ذكي** | نقطة استئناف بالثواني لكل درس، تقدم محفوظ، واختبارات تحدد استحقاق الشهادة |
| 👩‍🏫 | **حجز استشارات** | حجز → حفظ DB → إشعار FCM فوري للمعلمة → رقم مرجعي للمتعلّم → يظهر في إيرادات لوحة الإدارة |
| 📊 | **لوحة إدارة حية** | أرقام فعلية من قاعدة البيانات: الإيرادات، الحجوزات المعلّقة، سجل التدقيق |
| 🔐 | **حسابات آمنة** | تسجيل وتوثيق محليان مع تجزئة كلمات المرور salted |

---

## 🏗️ البنية المعمارية

```
┌─────────────────────────────────────────────────────┐
│  UI Layer — Jetpack Compose (RTL, Material 3)       │
│  screens/ + components/ + theme/  (439 نصاً في      │
│  └─ شاشات مقسمة: courses/ privatecall/ connect/     │
│     classroom/   strings.xml)                       │
├─────────────────────────────────────────────────────┤
│  Presentation — OrbitViewModel (Facade)             │
│  + MentalMathController · SocraticTutorController   │
│  + PrivateCallController · CheckoutController       │
├─────────────────────────────────────────────────────┤
│  Domain / Core                                      │
│  GeminiClient · OrbitCouponEngine ·                 │
│  OrbitPresenceRegistry · OrbitTimeFormatter ·       │
│  PasswordHasher                                     │
├─────────────────────────────────────────────────────┤
│  Data Layer                                         │
│  OrbitRepository (منطق) ← OrbitSeedData (كتالوج)    │
│  OrbitRoomRepository (facade) → Room DAOs           │
├─────────────────────────────────────────────────────┤
│  Services                                           │
│  FirebaseMessagingService → InAppSignalBus          │
│  OrbitNotificationManager (FCM + local channels)    │
└─────────────────────────────────────────────────────┘
```

**نقاط قوة التصميم:**
- ✅ تفكيك كتلة ViewModel أحادية إلى 4 controllers متخصصة مع API عام ثابت
- ✅ فصل تام: منطق الأعمال عن الكتالوج عن التخزين
- ✅ كل كتابة تمر عبر مسار واحد: Screen → ViewModel → Controller → DAO
- ✅ نصوص الواجهة كلها في `strings.xml` — طبقة المنطق locale-free
- ✅ حقن اعتماديات Hilt (`di/OrbitModules.kt`)

---

## 🔄 تكامل الميزات (Feature Synergy)

الميزات ليست جزراً منعزلة — كل واحدة تغذي الأخرى:

```
مشاهدة درس ──تقدم──▶ إكمال 100% ──▶ شهادة دائمة ──▶ تحقق عام
     │                                    │
     ▼                                    ▼
مساعد Gemini يفهم سياق الدرس        لوحة الإدارة تعرض الأرقام
     │
تسجيل FCM ──token──▶ Presence ──▶ دعوة مكالمة تصل فعلياً للطرف الآخر
     │                                    │
شراء دورة ──إيصال──▶ فتح المحتوى ◀──كوبون خصم حقيقي
لعبة XP ──تراكم ذري──▶ إنجازات تظهر في الملف الشخصي
```

---

## 🚀 البدء السريع

### المتطلبات
- Android Studio Ladybug+ مع Android SDK 36
- JDK 17+
- مشروع Firebase (لـ FCM) — اختياري للبناء التجريبي

### الخطوات
```bash
# 1) الاستنساخ
git clone <repo-url> && cd orbitlearn

# 2) توليد wrapper (غير مرفوع في المستودع)
gradle wrapper --gradle-version 8.9

# 3) إعداد الأسرار
cp .env.example .env
# ألغِ التعليق واملأ: GEMINI_API_KEY=...

# 4) (اختياري) ضع google-services.json في app/ لتفعيل FCM

# 5) البناء والتشغيل
./gradlew assembleDebug
./gradlew installDebug
```

### بدون مفتاح Gemini؟
التطبيق يعمل تلقائياً بـ **وضع دون اتصال** شفاف — المساعد يرد محلياً وتظهر شارة 🟢 بصدق. لا ادعاء كاذب بالاتصال.

---

## 🧪 الاختبارات

```bash
./gradlew test              # اختبارات الوحدة (سلامة Repository + Robolectric)
./gradlew connectedAndroidTest  # اختبارات instrumented على جهاز/emulator
```

**قائمة فحص ما قبل الدمج** (موثقة في الذاكرة المعمارية §8):
1. توازن أقواس Kotlin = صفري اختلال
2. صفر نتائج لبحث `simulate|mock|fake|dummy`
3. لا شاشة واحدة >600 سطراً دون تقسيم
4. أي نص عربي جديد يبدأ حياته في `strings.xml`

---

## 📦 مكدس التقنيات

| المجال | التقنية |
|---|---|
| UI | Jetpack Compose · Material 3 · Coil · CameraX |
| AI | Firebase AI (Generative AI SDK) — Gemini 2.0 Flash |
| Realtime | Firebase Cloud Messaging · Jitsi Meet WebRTC |
| Storage | Room SQLite (كائنات دائمة: مستخدمون، جلسات اتصال، شهادات، حجوزات، إيصالات، إنجازات، رسائل tutor، تقدم دروس، دورات) |
| DI | Hilt + KSP |
| Networking | Retrofit + Moshi |
| Build | Gradle KTS · Version Catalog (libs.versions.toml) |

---

## ⚠️ القيود المعروفة (بشفافية كاملة)

1. **وسائط P2P المباشرة** للمكالمات الفردية تتطلب خادم TURN/Signaling سحابياً — الـ signaling الآن حقيقي عبر FCM، والصوت/صورة يمران عبر بنية غرف Jitsi الموجودة.
2. **بوابة الدفع الخارجية** (Stripe/HyperPay) لم تُربط بعد — منطق الكوبونات والإيصالات والمحتوى المقفل حقيقي بالكامل، والتسوية البنكية تحتاج backend.
3. **google-services.json** غير مرفوع (سياسة أسرار) — FCM يُفعَّل بإسقاط الملف.

---

## 🗺️ خارطة الطريق

- [ ] ربط بوابة دفع فعلية مع webhooks للإيصالات
- [ ] خادم WebSocket/RTDB لوسائط P2P المباشرة
- [ ] اختبارات StateFlow snapshot لكل controllers
- [ ] Room schema migrations framework
- [ ] نسخة إنجليزية (values-en) لدعم i18n كامل

---

## 🤝 المساهمة

اقرأ أولاً **[PROJECT_MEMORY.md](.memory/PROJECT_MEMORY.md)** — خاصة «عقيدة التصميم: حقيقي لا محاكاة» (§3) و«عقد التكامل بين الميزات» (§5). أي PR يخالف قاعدة «مسار واحد للحقيقة» أو يدخل محاكاة يُرفض تلقائياً.

---

## 📄 الترخيص والدعم

ORBIT Learn 2026 — مبني بـ ❤️ بواسطة AI Studio. جميع النصوص العربية والواجهات RTL أصيلة.

</div>
