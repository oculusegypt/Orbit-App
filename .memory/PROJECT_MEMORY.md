# 🧠 الذاكرة المعمارية العميقة — تطبيق ORBIT Learn 2026
**Deep Project Memory | مدار للتعلم الرقمي**

> هذا الملف هو المرجع الهندسي الدائم للفريق والمطورين والمساعدين الذكيين.
> آخر تحديث: نوفمبر 2026 — بعد اكتمال دورة إعادة الهيكلة والتحويل من Mock إلى حقيقي بالكامل.

---

## 1️⃣ الهوية التقنية (Identity Card)

| البند | القيمة |
|---|---|
| اسم التطبيق | ORBIT Learn 2026 \| مدار للتعلم الرقمي |
| معرّف الحزمة | `com.aistudio.orbitlearn.ekjvpt` |
| حزمة الكود | `com.example.*` (إرث AI Studio — لم تُغيَّر لتجنب كسر google-services.json) |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36.1 |
| اللغة الأساسية | Kotlin 2.x + Jetpack Compose (UI كامل بدون XML layouts) |
| capability المعلن | `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` |
| حقن الاعتماديات | Hilt (`di/OrbitModules.kt`) |
| قاعدة البيانات | Room SQLite — جداول دائمة متعددة |
| الرسائل الفورية | Firebase Cloud Messaging (topics: `user_<id>` لكل جهاز) |
| الفصول الحية | Jitsi Meet عبر WebView + WebRTC حقيقي (كاميرا/ميكروفون فعليان) |
| الذكاء الاصطناعي | Gemini 2.0 Flash عبر Generative AI SDK مع سياق سقراطي متدفق |
| عدد ملفات Kotlin | 80 ملفاً في طبقات نظيفة |
| الموارد النصية | 439 نصاً عربياً في `strings.xml` (RTL كامل) |

---

## 2️⃣ خريطة الطبقات المعمارية (Layer Map)

```
com/example/
├── MainActivity.kt            ← نقطة الدخول + ربط حالة Gemini بالـ UI
├── OrbitApplication.kt        ← @HiltAndroidApp + تهيئة FCM channels
├── core/                      ← أدوات مجرّدة قابلة لإعادة الاستخدام
│   ├── GeminiClient.kt        ← عميل Generative AI (حل مفتاح: BuildConfig → Secure Setting)
│   ├── OrbitCouponEngine.kt   ← محرك كوبونات الخصم (منطق تجاري حقيقي)
│   ├── OrbitPresenceRegistry.kt ← سجل الحضور الحي للأجهزة (token ↔ user)
│   ├── OrbitTimeFormatter.kt  ← التوحيد المركزي لتنسيق التاريخ/الوقت العربي
│   └── PasswordHasher.kt      ← تجزئة كلمات المرور (salted hash)
├── data/
│   ├── local/                 ← Room: DAOs + Entities
│   │   ├── OrbitDatabase.kt   ← قاعدة البيانات المركزية (version-controlled)
│   │   ├── UserAccount*       ← حسابات مسجّلة موثّقة فعلياً
│   │   ├── CallSession*       ← سجل مكالمات بدورات حياة كاملة
│   │   ├── Certificate*       ← شهادات دائمة survive restarts
│   │   ├── ConsultationBooking* ← حجوزات استشارات موحّدة المسار
│   │   ├── PurchaseReceipt*   ← إيصالات شراء دائمة
│   │   ├── LearnerAchievement* ← XP/Streak تراكم ذري (accumulate upsert)
│   │   ├── TutorMessage*      ← محادثات Gemini السقراطية محفوظة
│   │   ├── LessonProgress*    ← نقطة استئناف بالثواني لكل درس
│   │   └── Course*            ← كتالوج الدورات المحلي
│   ├── models/OrbitModels.kt  ← نماذج المجال المشتركة
│   └── repository/
│       ├── OrbitRepository.kt     ← singleton منطق الأعمال (أُفرغ من الكتالوج)
│       ├── OrbitRoomRepository.kt ← facade فوق DAOs
│       └── OrbitSeedData.kt       ← كتالوج المحتوى الابتدائي (انعزل عن المنطق)
├── services/
│   ├── OrbitFirebaseMessagingService.kt ← استقبال push → OrbitInAppSignalBus
│   ├── OrbitNotificationManager.kt      ← مجموعات إشعارات + dispatchCallInvite()
│   └── OrbitInAppSignalBus.kt           ← ناقل إشارات داخل-تطبيقي (SharedFlow)
├── di/OrbitModules.kt         ← وحدات Hilt (Database, DAOs, Repo providers)
└── ui/
    ├── screens/               ← الشاشات مقسّمة إلى وحدات فرعية (انظر §4)
    ├── components/            ← حوارات ومكونات معاد استخدامها
    ├── theme/                 ← Color/Type/Theme هوية مدار البصرية
    └── viewmodel/             ← OrbitViewModel Facade + controllers متخصصة
```

---

## 3️⃣ عقيدة التصميم — "حقيقي لا محاكاة" (Truth Doctrine)

### ✅ ما تحوّل من Mock إلى حقيقي (مع الأدلة)

| الميزة | قبل | بعد | الدليل في الكود |
|---|---|---|---|
| **المساعد السقراطي** | ردود ثابتة when/contains | Gemini 2.0 Flash فعلي بـ system instruction سقراطي + آخر 10 رسائل كسياق، fallback دون اتصال فقط عند الفشل | `core/GeminiClient.kt`, `SocraticTutorController.sendQuery` |
| **شارة المحرك** | ثابتة | `viewModel.isAiTutorLive → MainActivity` ديناميكية | `MainActivity.kt` |
| **دعوة المكالمة الصادرة** | حلقة محلية صامتة | `dispatchCallInvite()` تنشر على topic FCM `user_<id>` وترفض بصراحة بلا token | `OrbitNotificationManager`, `PrivateCallController` |
| **الشهادات** | قائمة volatile | حفظ Room `persistCertificate` + بحث ثنائي `findDurableCertificate` + دمج تدفق catalog↔Room بإزالة تكرار | `OrbitViewModel.openVerifier/verifyCertificateInput` |
| **XP اللعبة** | يضيع عند الإغلاق | `recordRoundOutcome` → `LearnerAchievementDao.accumulate` تراكم ذري | `OrbitViewModel.submitMentalMathAnswer` |
| **الحجوزات والإيصالات والمحادثات** | in-memory | كلها جداول Room دائمة | `data/local/*Entity.kt` |
| **لوحة الإدارة** | أرقام وهمية ثابتة | إيرادات وحجوزات معلّقة فعلية من قاعدة البيانات | `AdminScreen.kt` |
| **الدفع** | SARA2026 hardcoded مباشر | `applyCouponCode`/`completeCheckout` عبر ViewModel + محرك كوبونات + تسجيل إيصال | `CheckoutController`, `OrbitCouponEngine` |

### 🔴 قواعد صارمة راسخة (لا تُكسر)
1. **صفر محاكاة**: البحث الشامل عن `simulate|mock|fake|dummy` في الكود = صفر نتائج. ممنوع إدخال أي زر تمثيلي.
2. **شفافية الفشل**: عند غياب الشبكة/المفتاح، يُعرض "وضع دون اتصال" صراحةً — لا تمثيل نجاح كاذب.
3. **مسار واحد للحقيقة**: كل عملية كتابة تمر عبر ViewModel → Controller → DAO. ممنوع أن تكتب الشاشة مباشرةً في Repository (أُغلقت ثغرة "المسار المزدوج" في الحجوزات).
4. **locale-free literals**: طبقة ViewModel/Controllers لا تحتوي نصوصاً للمستخدم؛ كل النصوص العربية في `strings.xml` وتُقرأ بـ `stringResource`.
5. **API ثابت للـ ViewModel**: أثناء كل عمليات التفكيك، حافظ `OrbitViewModel` على واجهته العامة (الخصائص/التدفقات) كما هي حتى لا تنكسر الشاشات المستهلكة.

---

## 4️⃣ تاريخ إعادة الهيكلة (Refactor Lineage)

| الجولة | الإنجاز | القياس |
|---|---|---|
| R1 | تفكيك `OrbitViewModel` أحادي الكتلة (849 سطراً) إلى MentalMath/SocraticTutor/PrivateCall/Checkout controllers + مركزة الوقت في `OrbitTimeFormatter` | VM ↓ إلى ~651 سطراً، API عام unchanged |
| R2 | تفعيل Gemini الحقيقي بدل mock المساعد + شارة شفافية الحالة | +`GeminiClient.kt` |
| R3 | ربط MainActivity بمعالجات دفع حقيقية | إزالة mock checkout |
| R4 | تحويل الاستشارة إلى مسار حقيقي: حفظ DB + FCM فوري + رقم مرجعي + لوحة إدارة بأرقام فعلية | |
| R5 | إصلاح فقدان البيانات عند إعادة التشغيل (حجوزات/إيصالات/XP/محادثات) → Room | |
| R6 | تقسيم الشاشات الضخمة: `VirtualClassroom` (1093→646)، `PrivateCallScreen`، `CoursesScreen`، `PrivateConnectScreen` إلى حزم فرعية | -41% للملف الأكبر |
| R7 | عزل `OrbitSeedData.kt` عن `OrbitRepository.kt` + استعادة الدورة الرابعة المفقودة من سجل Git | |
| R8 | نقل 439 نصاً عربياً إلى `strings.xml` في 37 ملف UI (commit `9c190fd`) | |
| R9 | signaling حقيقي للاتصال outbound عبر FCM topics | |

### حزم UI الفرعية الناتجة (Structural Convention)
```
ui/screens/courses/     → CourseCatalog, CourseDetail, CourseQuiz, CourseVerifier, LessonPlayerTab
ui/screens/privatecall/ → ActiveCallRoomView, CallEndedView, CallRingingViews, ScreenShareWorkspace
ui/screens/connect/     → CallHistoryCard, UserContactCard
ui/screens/classroom/   → ClassroomStageViews (519), LiveWebRTCMeetingRoom, LiveAudioMonitor,
                          LiveCameraPreview, BreakoutRoomPickerDialog
```
**القاعدة:** الشاشة الأم تحتفظ بالحالة والتنسيق؛ المكوّنات الفرعية stateless تستقبل callbacks. أي شاشة جديدة >600 سطراً يجب أن تُقسَّم بهذا النمط.

---

## 5️⃣ عقد التكامل بين الميزات (Cross-Feature Logic Contracts)

هذا هو جوهر "التجاور المنطقي" — كيف تتغذى الميزات على بعضها:

```
[مشاهدة درس] --LessonProgress(ثوانٍ)--> [استئناف تلقائي] --> [إكمال 100%]
      │                                                    │
      ▼                                                    ▼
[Tutor Gemini يفهم سياق الدرس الحالي]          [شهادة → persistCertificate(Room)]
                                                        │
[FCM register] --token--> [PresenceRegistry] <-----------┤ verify via findDurableCertificate
                              │                          │
                    [dispatchCallInvite]                 ▼
                              │                  [AdminScreen: أرقام فعلية]
                    [inbound push → SignalBus → PrivateCallController.ring]
                              │
              [قبول/رفض → CallSessionDao.save(duration,status)]
                              │
[ConsultationBooking] --حجز--> [رقم مرجعي + FCM للمعلمة] --إتمام--> [يؤثر في إيرادات Admin]
[Checkout + CouponEngine] --> [PurchaseReceipt Dao] --> [فتح محتوى الدورة في Catalog]
[MentalMathGame rounds] --> [accumulate XP atomic] --> [LearnerAchievement يظهر في Home/Badge]
```

### نقاط الربط الحرجة (إذا انكسرت، تنكسر سلسلة كاملة)
| # | العقد | الموقع |
|---|---|---|
| C1 | `attachGeminiClient()` يُستدعى من `initializeServices(context)` قبل أول رسالة tutor | OrbitViewModel |
| C2 | `contextProvider = { appContext }` ممرَّر لـ PrivateCallController (افتراضي `{null}` يُعطّل الـ dispatch بصمت!) | OrbitViewModel |
| C3 | `submitMentalMathAnswer` يستدعي `recordRoundOutcome(viewModelScope, userId)` عند RESULT | OrbitViewModel |
| C4 | `val submissions get() = OrbitRepository.submissions` — المستهلك الوحيد: LearnerScreen:43, InstructorScreen:39 | OrbitViewModel |
| C5 | FCM data message → `OrbitInAppSignalBus` SharedFlow → `deliverIncomingCall` — نفس المسار للاتجاهين | services/* |
| C6 | `isAiTutorLive` ينعكس في شارة SocraticTutorSheet (🟡 متصل / 🟢 دون اتصال) | MainActivity |

---

## 6️⃣ الأسرار والمفاتيح (Secrets Protocol)

- **GEMINI_API_KEY**: ترتيب الحل داخل `GeminiClient`:
  1. `BuildConfig.GEMINI_API_KEY` (يُحقن من `.env` عبر plugin أسرار AI Studio — السطر `# GEMINI_API_KEY=` في `.env.example` يلغى تعليقه)
  2. Secure Setting للتجربة المحلية
  3. فشل آمن → وضع offline مع شارة صريحة. **ممنوع رفع `.env` في Git.**
- **google-services.json**: غائب في المستودع (سياسة WARN). البناء من Android Studio يعمل؛ CI يحتاج الحقن.
- **gradlew**: سكربتات wrapper غير مولّدة في البيئة الحالية — `gradle wrapper` ضروري قبل CI.

---

## 7️⃣ قيود بيئية موثّقة (Known Environment Limits)

1. ❌ لا Android SDK في بيئة التطوير الحالية → الترجمة الكاملة تتطلب `./gradlew assembleDebug` محلياً أو في Studio. التحققات هنا كانت: توازن أقواس آلي (صفر اختلال)، سلامة مراجع، مطابقة أنماط الأنواع.
2. ⚠ P2P media فعلي لمكالمات 1-on-1 يتطلب خادم TURN/Signaling سحابي — البنية ليست قرار client-side. المسار signalling الآن حقيقي عبر FCM؛ الـ media يعتمد غرف Jitsi الموجودة.
3. ⚠ بوابة دفع خارجية (Stripe/HyperPay) لم تُربط — الكوبونات والإيصالات والمنطق حقيقي لكن التسوية البنكية تحتاج backend.

---

## 8️⃣ الاختبارات والصيانة (Test & Maintenance Anchors)

- `app/src/test/…/ExampleUnitTest.kt` — فحص سلامة بيانات Repository.
- Robolectric + androidTest موجودان كهيكل.
- **قبل أي PR كبير**: تحقق التوازن الآلي للأقواس، ابحث عن `simulate|mock` (يجب صفر)، تأكد أن شاشات >600 سطراً مقسّمة، وأن أي نص عربي جديد يولد في strings.xml أولاً.

## 9️⃣ خارطة الطريق المتبقية (Backlog)
1. ربط بوابة دفع حقيقية (Stripe/HyperPay) مع webhook receipts.
2. خادم signaling WebSocket/RTDB لوسائط P2P المباشرة.
3. اختبارات وحدة للـ Controllers (StateFlow snapshot tests).
4. Migration framework للاختبارات عند تغيير schema Room.
5. i18n: نسخة إنجليزية موازية لـ values-en.
