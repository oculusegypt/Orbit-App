package com.example.data.repository
import com.example.core.OrbitTimeFormatter

import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.Locale

object OrbitRepository {

    val INSTRUCTOR_SARA = Instructor(
        id = "inst-sara-elarby",
        name = "أ / سارة العربي",
        title = "معلمة تأسيس أطفال (عربي ورياضة) ومدربة حساب ذهني معتمدة",
        bio = "متخصصة في تأسيس الأطفال في القراءة واللغة العربية والحساب الذهني بالمنصورة. شعارنا: تأسيس أطفال . حساب ذهني 💡 وبطريقة ممتعة وسهلة للأطفال.",
        avatarUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80",
        phone = "010 28340042",
        whatsapp = "+201028340042",
        email = "Saraelarby96@gmail.com",
        location = "المنصورة، الدقهلية، مصر"
    )

    val PRESET_USERS = listOf(
        User(
            id = "usr-student-1",
            email = "yassine.ahmed@orbit.edu",
            displayName = "ياسين أحمد (بطل العباقرة)",
            role = UserRole.LEARNER,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80"
        ),
        User(
            id = "inst-sara-elarby",
            email = "Saraelarby96@gmail.com",
            displayName = "أ / سارة العربي",
            role = UserRole.INSTRUCTOR,
            avatarUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80"
        ),
        User(
            id = "usr-admin-1",
            email = "admin@orbit2026.edu",
            displayName = "م. عمر المنصوري (إدارة المنصة)",
            role = UserRole.ADMIN,
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80"
        )
    )

    private val initialCourses = listOf(
        Course(
            id = "course-soroban-kids",
            slug = "soroban-mental-math-for-kids",
            title = "دبلوم الحساب الذهني وتنمية الذكاء للأطفال (Soroban & Mental Math)",
            summary = "منهج تفاعلي شيق وممتع للأطفال لتعلم الحساب الذهني باستخدام العداد الصيني، ومضاعفة سرعة التركيز والتخيل وإجراء العمليات الحسابية بسرعة خارقة بدون آلة حاسبة.",
            category = "الحساب الذهني وتنمية الذكاء",
            level = "مبتدئ إلى متقدم (6 - 12 سنة)",
            priceMinor = 25000,
            currency = "EGP",
            rating = 4.98,
            reviewsCount = 184,
            durationHours = 12.0,
            thumbnailUrl = "https://images.unsplash.com/photo-1596495578065-6e0763fa1178?auto=format&fit=crop&w=1200&q=80",
            instructor = INSTRUCTOR_SARA,
            outcomes = listOf(
                "التعرف على أجزاء العداد الصيني (السوروبان) وقيمة كل خرزة في الآحاد والعشرات",
                "إتقان عمليات الجمع والطرح المباشر بحركات الأصابع السريعة (السبابة والإبهام)",
                "تطبيق قواعد أصدقاء العدد 5 الصغار (Small Friends) باحتراف وسهولة",
                "تطبيق قواعد أصدقاء العدد 10 الكبار (Big Friends) في المسائل المركبة",
                "الحساب التخيلي (Anzan) بدون استخدام العداد الحقيقي وبسرعة مبهرة",
                "مضاعفة التركيز البصري والسمعي وسرعة البديهة وحب مادة الرياضيات"
            ),
            prerequisites = listOf(
                "معرفة الطفل بالأرقام وقراءتها وكتابتها من 0 إلى 20",
                "حماس الطفل للتعلم باللعب والتجربة، وتوفير عداد صيني صغير للتدريب العملي"
            ),
            sections = listOf(
                CourseSection(
                    id = "sec-soroban-1",
                    courseId = "course-soroban-kids",
                    title = "الوحدة الأولى: التعرف على العداد الصيني والجمع والطرح المباشر",
                    position = 1,
                    lessons = listOf(
                        Lesson(
                            id = "les-soroban-1-1",
                            sectionId = "sec-soroban-1",
                            title = "الدرس 1: رحلة في عالم العداد الصيني وقصة الخرزات السحرية",
                            lessonKind = "video",
                            durationSeconds = 420,
                            position = 1,
                            isPreview = true,
                            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                            bodyMarkdown = """### مرحباً بكم يا أبطال مع مس سارة العربي 💡
العداد الصيني (السوروبان) هو أداة يابانية وصينية عبقرية تساعد عقل الطفل على تخيل الأرقام كأشكال ملموسة بدلاً من مجرد أرقام جافة!

#### مكونات العداد:
1. **الإطار الخارجي (Frame):** يحمي الخرزات.
2. **العارضة الفاصلة (Beam):** تفصل بين الخرزات العلوية والسفلية، وقراءة الأرقام تحدث فقط عندما تلمس الخرزة هذه العارضة.
3. **الخرزة العلوية (Upper Bead):** خرزة السماء وقيمتها = **5**.
4. **الخرزات الأربعة السفلية (Lower Beads):** خرزات الأرض وقيمة كل واحدة منها = **1**.
5. **نقطة البداية (Unit Dot):** عمود الآحاد.

> قاعدة مس سارة الذهبية: "تأسيس أطفال . حساب ذهني.. حرك الخرزات بحب وشوف النتيجة قدام عينيك!"
                            """.trimIndent(),
                            transcript = listOf(
                                TranscriptLine("t1", 0, "أ/ سارة العربي", "أهلاً بيكم يا أبطال في أول حصة من حصص الحساب الذهني الممتعة مع مس سارة!"),
                                TranscriptLine("t2", 25, "أ/ سارة العربي", "شايفين العداد الجميل ده؟ ده مش مجرد لعبة، ده مفتاح سحري هيخليك تحسب أسرع من الآلة الحاسبة!"),
                                TranscriptLine("t3", 80, "أ/ سارة العربي", "العارضة الفاصلة دي هي خط القراءة.. أي خرزة تلمسها بنحسبها على طول."),
                                TranscriptLine("t4", 160, "أ/ سارة العربي", "الخرزة الفوقانية لوحدها بتساوي 5، والأربعة اللي تحت كل واحدة بـ 1."),
                                TranscriptLine("t5", 280, "أ/ سارة العربي", "يلا نجرب نرفع خرزة واحدة بإصبع الإبهام.. كده بقى معانا 1! شطار يا عباقرة!")
                            ),
                            resources = listOf(
                                ResourceAttachment("r1", "ورقة عمل ملونة: التعرف على أجزاء العداد وتلوين الخرزات (PDF)", "pdf", "1.2 MB"),
                                ResourceAttachment("r2", "دليل ولي الأمر لمتابعة تمارين العداد في المنزل", "pdf", "850 KB")
                            ),
                            quiz = QuizData(
                                id = "quiz-soroban-1",
                                title = "اختبار العباقرة الصغار: التعرف على العداد الصيني",
                                passingScore = 80,
                                questions = listOf(
                                    QuizQuestion(
                                        id = "q1",
                                        prompt = "كم تساوي الخرزة العلوية الواحدة (خرزة السماء) في عمود الآحاد؟",
                                        options = listOf("1", "5", "10", "0"),
                                        correctIndex = 1,
                                        explanation = "شاطر يا بطل! الخرزة العلوية في السوروبان تساوي دائماً 5."
                                    ),
                                    QuizQuestion(
                                        id = "q2",
                                        prompt = "ما هو الإصبع المخصص لرفع خرزات الأرض السفلية في عملية الجمع؟",
                                        options = listOf("السبابة", "الإبهام", "الخنصر", "راحة اليد"),
                                        correctIndex = 1,
                                        explanation = "ممتاز! نستخدم دائماً إصبع الإبهام لرفع الخرزات السفلية للأعلى."
                                    ),
                                    QuizQuestion(
                                        id = "q3",
                                        prompt = "متى تصبح قيمة الخرزة محسوبة في الناتج؟",
                                        options = listOf(
                                            "عندما تبتعد عن العارضة الفاصلة",
                                            "عندما تلمس العارضة الفاصلة (خط الحساب)",
                                            "عندما تسقط لأسفل الإطار",
                                            "عند هز العداد بالكامل"
                                        ),
                                        correctIndex = 1,
                                        explanation = "برافو! العارضة الفاصلة هي خط القراءة، والخرزة لا تُحسب إلا إذا لامستها."
                                    ),
                                    QuizQuestion(
                                        id = "q4",
                                        prompt = "إذا لامست خرزة السماء العارضة، وخرزتان من الأسفل لامستا العارضة، فما الرقم؟",
                                        options = listOf("2", "5", "7", "8"),
                                        correctIndex = 2,
                                        explanation = "عبقري! 5 (خرزة السماء) + 2 (خرزات الأرض) = 7!"
                                    )
                                )
                            )
                        ),
                        Lesson(
                            id = "les-soroban-1-2",
                            sectionId = "sec-soroban-1",
                            title = "الدرس 2: الجمع والطرح المباشر على عمود الآحاد (1 إلى 9)",
                            lessonKind = "video",
                            durationSeconds = 540,
                            position = 2,
                            isPreview = false,
                            bodyMarkdown = """### قواعد الحركة السريعة بالأصابع 🖐️
- **الجمع المباشر (+1 إلى +4):** نرفع الخرزات السفلية بإصبع **الإبهام** نحو العارضة.
- **الطرح المباشر (-1 إلى -4):** ننزل الخرزات السفلية بإصبع **السبابة** بعيداً عن العارضة.
- **العدد 5 (+5 و -5):** نحرك خرزة السماء العلوية نزولاً وصعوداً بإصبع **السبابة** فقط!

#### تمرين تفاعلي:
حل المسألة التالية: `2 + 2 - 1 + 5 = ?`
الخطوات:
1. ارفع خرزتين بالإبهام (2).
2. ارفع خرزتين إضافيتين بالإبهام (صاروا 4).
3. نزل خرزة واحدة بالسبابة (صاروا 3).
4. نزل خرزة السماء بالسبابة (3 + 5 = **8**).
                            """.trimIndent(),
                            transcript = listOf(
                                TranscriptLine("t2-1", 0, "أ/ سارة العربي", "أهلاً بأبطال الحساب الذهني! جاهزين نلعب بأصابعنا السحرية؟"),
                                TranscriptLine("t2-2", 45, "أ/ سارة العربي", "افتكروا دايماً: الإبهام للجمع، والسبابة للطرح، والسبابة برضه لخرزة الـ 5 العلوية.")
                            ),
                            resources = listOf(
                                ResourceAttachment("r3", "كراسة تمارين: 50 مسألة جمع وطرح مباشر مع مس سارة", "pdf", "1.8 MB")
                            )
                        )
                    )
                ),
                CourseSection(
                    id = "sec-soroban-2",
                    courseId = "course-soroban-kids",
                    title = "الوحدة الثانية: أصدقاء العدد 5 الصغار (Small Friends)",
                    position = 2,
                    lessons = listOf(
                        Lesson(
                            id = "les-soroban-2-1",
                            sectionId = "sec-soroban-2",
                            title = "الدرس 3: قصة أصدقاء العدد 5 وسر الخرزة الكبيرة (+1, +2, +3, +4)",
                            lessonKind = "video",
                            durationSeconds = 620,
                            position = 1,
                            isPreview = false,
                            bodyMarkdown = """### أصدقاء العدد 5 السحريين!
لما نكون عاوزين نضيف رقم ومش لاقيين خرزات كافية تحت، بنستعين بالصديق الكبير (الخرزة 5)!
- `+4 = +5 - 1`
- `+3 = +5 - 2`
- `+2 = +5 - 3`
- `+1 = +5 - 4`
                            """.trimIndent()
                        )
                    )
                )
            )
        ),
        Course(
            id = "course-arabic-foundations",
            slug = "arabic-reading-nour-al-bayan",
            title = "تأسيس اللغة العربية والقرائية الممتعة للأطفال (نور البيان والفهم السريع)",
            summary = "كورس تأسيس شامل ومحبب للطفل من الصفر حتى طلاقة القراءة والكتابة، معتمد على طريقة نور البيان والتحليل الصوتي الممتع بدون تعقيد.",
            category = "تأسيس اللغة العربية والقرائية",
            level = "تأسيس تمهيدي وابتدائي (4 - 9 سنوات)",
            priceMinor = 20000,
            currency = "EGP",
            rating = 4.95,
            reviewsCount = 142,
            durationHours = 10.0,
            thumbnailUrl = "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=1200&q=80",
            instructor = INSTRUCTOR_SARA,
            outcomes = listOf(
                "نطق الحروف العربية بأصواتها الصحيحة ومخارجها الدقيقة",
                "التمييز البصري والسمعي بين أشكال الحروف في أول ووسط وآخر الكلمة",
                "قراءة الكلمات الثلاثية بحركة الفتح والكسر والضم بطلاقة وسرعة",
                "التحليل الصوتي والتهجي الذكي للكلمات وتحديد المد والممدود",
                "كتابة الإملاء بدون تردد وتنمية ثقة الطفل في التعبير اللغوي"
            ),
            prerequisites = listOf(
                "مناسب للأطفال من عمر 4 سنوات بدون أي معرفة مسبقة بالحروف",
                "رغبة ولي الأمر في المتابعة التفاعلية اليومية لمدة 15 دقيقة فقط"
            ),
            sections = listOf(
                CourseSection(
                    id = "sec-arabic-1",
                    courseId = "course-arabic-foundations",
                    title = "الوحدة الأولى: أصوات الحروف وحركة الفتح السحرية",
                    position = 1,
                    lessons = listOf(
                        Lesson(
                            id = "les-ar-1",
                            sectionId = "sec-arabic-1",
                            title = "الدرس 1: حكاية حركة الفتح السحرية وقراءة أول كلمة (كـ - تـ - ب)",
                            lessonKind = "video",
                            durationSeconds = 480,
                            position = 1,
                            isPreview = true,
                            bodyMarkdown = """### قصة حركة الفتح مع مس سارة 💡
الفتحة هي شرطة صغيرة ذكية فوق الحرف، بتخلي الحرف يفتح فمه ويبتسم ويقول:
- أَ ... بَ ... تَ ... ثَ ... جَ ... حَ ... خَ

#### دمج الأصوات:
- كَ + تَ + بَ = **كَتَبَ**
- قَ + رَ + أَ = **قَرَأَ**
- زَ + رَ + عَ = **زَرَعَ**
                            """.trimIndent(),
                            resources = listOf(
                                ResourceAttachment("ra1", "كروت الحروف الملونة بحركة الفتح للتلوين والقص (PDF)", "pdf", "2.1 MB")
                            ),
                            quiz = QuizData(
                                id = "quiz-arabic-1",
                                title = "تحدي القراءة السريعة بالفتح مع مس سارة",
                                passingScore = 80,
                                questions = listOf(
                                    QuizQuestion(
                                        id = "qa1",
                                        prompt = "ما هي الكلمة الصحيحة الناتجة عن دمج أصوات: (ذَ - هَ - بَ)؟",
                                        options = listOf("ذَهَبَ", "هَذَبَ", "بَذَهَ", "ذُهِبَ"),
                                        correctIndex = 0,
                                        explanation = "شاطر جداً! ذَ + هَ + بَ = ذَهَبَ."
                                    ),
                                    QuizQuestion(
                                        id = "qa2",
                                        prompt = "أين توضع حركة الفتح وماذا نفعل عند نطقها؟",
                                        options = listOf(
                                            "توضع تحت الحرف ونكسر فمنا",
                                            "توضع فوق الحرف ونفتح فمنا قليلاً بابتسامة",
                                            "توضع بجانب الحرف ونضم شفاهنا",
                                            "لا توضع أبداً"
                                        ),
                                        correctIndex = 1,
                                        explanation = "ممتاز! الفتحة شرطة مائلة فوق الحرف ونفتح معها الفم."
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ),
        Course(
            id = "course-smart-math",
            slug = "smart-math-elementary-kids",
            title = "تأسيس الرياضيات الذكية والممتعة للأطفال (Smart Math)",
            summary = "برنامج تأسيس الرياضيات المدرسية الحديثة: فهم القيمة المكانية، مكونات الأعداد، الجمع والطرح الاستراتيجي، وحل المسائل اللفظية بدون حفظ.",
            category = "الرياضيات المدرسية والتفكير المنطقي",
            level = "تمهيدي ومرحلة ابتدائية (5 - 10 سنوات)",
            priceMinor = 18000,
            currency = "EGP",
            rating = 4.96,
            reviewsCount = 98,
            durationHours = 8.5,
            thumbnailUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?auto=format&fit=crop&w=1200&q=80",
            instructor = INSTRUCTOR_SARA,
            outcomes = listOf(
                "فهم عميق للآحاد والعشرات والقيمة المكانية للأعداد",
                "استراتيجيات الجمع بمكونات العدد 10 بدون العد على الأصابع",
                "حل المسائل الكلامية الحياتية بتخيل القصة والرسم",
                "التعرف على الأشكال الهندسية والأنماط التفكيرية"
            ),
            prerequisites = listOf(
                "معرفة الأعداد من 1 إلى 20 وكتابتها"
            ),
            sections = listOf(
                CourseSection(
                    id = "sec-math-1",
                    courseId = "course-smart-math",
                    title = "الوحدة الأولى: بيت الآحاد وبيت العشرات ومكونات الأعداد",
                    position = 1,
                    lessons = listOf(
                        Lesson(
                            id = "les-math-1",
                            sectionId = "sec-math-1",
                            title = "الدرس 1: قصة بيت الآحاد الصغير وقصر العشرات الفخم",
                            lessonKind = "video",
                            durationSeconds = 380,
                            position = 1,
                            isPreview = true,
                            bodyMarkdown = "### قصة بيت الآحاد وقصر العشرات..\nأي رقم يقف في بيت الآحاد قيمته هي هي، أما في قصر العشرات بيزيد صفر على اليمين!"
                        )
                    )
                )
            )
        ),
        Course(
            id = "course-tot-trainers",
            slug = "tot-mental-math-trainers",
            title = "دبلوم تدريب وإعداد مدربي الحساب الذهني المعتمدين (TOT للأمهات والمعلمات)",
            summary = "الدورة الاحترافية المعتمدة لإعداد وتأهيل مدربات الحساب الذهني والسوروبان، واكتساب استراتيجيات التدريس النشط، وإدارة الفصول وتأسيس الأكاديميات الخاصة.",
            category = "إعداد وتأهيل المدربين والمعلمات",
            level = "للمعلمات والأمهات وخريجي الجامعات",
            priceMinor = 45000,
            currency = "EGP",
            rating = 5.0,
            reviewsCount = 76,
            durationHours = 20.0,
            thumbnailUrl = "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?auto=format&fit=crop&w=1200&q=80",
            instructor = INSTRUCTOR_SARA,
            outcomes = listOf(
                "إتقان جميع مستويات العداد الصيني والعمليات الحسابية المتقدمة",
                "سيكولوجية الطفل والتعامل مع صعوبات التعلم والتشتت",
                "تصميم أوراق العمل والحقائب التدريبية التفاعلية",
                "الحصول على شهادة مدرب حساب ذهني معتمد من أكاديمية مدار"
            ),
            prerequisites = listOf(
                "حب العمل مع الأطفال والرغبة في التمكين المهني أو التدريب المنزلي"
            ),
            sections = listOf(
                CourseSection(
                    id = "sec-tot-1",
                    courseId = "course-tot-trainers",
                    title = "الوحدة الأولى: منهجية برنامج العباقرة الصغار وسيكولوجية تدريب الأطفال",
                    position = 1,
                    lessons = listOf(
                        Lesson(
                            id = "les-tot-1",
                            sectionId = "sec-tot-1",
                            title = "الدرس 1: أسرار تدريب الأطفال وتحويل الرياضيات إلى متعة وشغف",
                            lessonKind = "video",
                            durationSeconds = 720,
                            position = 1,
                            isPreview = true,
                            bodyMarkdown = "### أسرار نجاح مدرب الحساب الذهني..\n1. كسر حاجز الخوف من الأرقام.\n2. استخدام الألعاب الحركية.\n3. التحفيز المستمر وتتويج العباقرة الصغار."
                        )
                    )
                )
            )
        )
    )

    private val initialBooks = listOf(
        LibraryBook(
            id = "book-soroban-guide",
            title = "دليل العباقرة الصغار في الحساب الذهني والسوروبان",
            author = "أ / سارة العربي",
            category = "الحساب الذهني وتنمية الذكاء",
            pagesCount = 140,
            coverUrl = "https://images.unsplash.com/photo-1596495578065-6e0763fa1178?auto=format&fit=crop&w=600&q=80",
            description = "المرجع الشامل للأطفال والمدربين لتعلم خوارزميات العداد الصيني، أصدقاء العدد 5 والعدد 10، وتمارين الحساب التخيلي السريع بدون آلة حاسبة.",
            downloadUrl = "https://wa.me/201028340042",
            publishedYear = 2026,
            chapters = listOf(
                BookChapter(1, "الفصل الأول: مدخل إلى عالم السوروبان وحركة الخرزات", "1-25"),
                BookChapter(2, "الفصل الثاني: الجمع والطرح المباشر وتدريبات الأصابع", "26-55"),
                BookChapter(3, "الفصل الثالث: أصدقاء العدد 5 الصغار (Small Friends)", "56-85"),
                BookChapter(4, "الفصل الرابع: أصدقاء العدد 10 الكبار (Big Friends)", "86-115"),
                BookChapter(5, "الفصل الخامس: تقنيات الحساب التخيلي (Anzan) وسرعة الاستجابة", "116-140")
            )
        ),
        LibraryBook(
            id = "book-nour-al-bayan",
            title = "سلسلة الفهم مش الحفظ في تأسيس القرائية ونور البيان",
            author = "أ / سارة العربي",
            category = "تأسيس اللغة العربية والقرائية",
            pagesCount = 160,
            coverUrl = "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=600&q=80",
            description = "منهج تطبيقي لتأسيس الطفل في قراءة الكلمات الثلاثية، التحليل الصوتي للمقاطع، والتمييز بين المدود الطويلة والحركات القصيرة بالفهم والتشويق 💡.",
            downloadUrl = "https://wa.me/201028340042",
            publishedYear = 2026,
            chapters = listOf(
                BookChapter(1, "الفصل الأول: مخارج الحروف وأصواتها الهجائية بالفتح", "1-35"),
                BookChapter(2, "الفصل الثاني: دمج الحروف وقراءة الكلمات الثلاثية بالفتح", "36-70"),
                BookChapter(3, "الفصل الثالث: حركتا الكسر والضم وقراءة الجمل البسيطة", "71-110"),
                BookChapter(4, "الفصل الرابع: المدود الطويلة (أ، و، ي) والتحليل الصوتي المتقن", "111-160")
            )
        ),
        LibraryBook(
            id = "book-smart-math",
            title = "كتاب الرياضيات الذكية والأنشطة التفاعلية للأطفال",
            author = "أ / سارة العربي",
            category = "الرياضيات الذكية والتفكير المنطقي",
            pagesCount = 120,
            coverUrl = "https://images.unsplash.com/photo-1518133835878-5a93cc3f89e5?auto=format&fit=crop&w=600&q=80",
            description = "أنشطة وقصص مصورة لتوضيح القيمة المكانية (بيت الآحاد وقصر العشرات)، استراتيجيات الجمع الذهني ومكونات العدد 10 دون العد على الأصابع.",
            downloadUrl = "https://wa.me/201028340042",
            publishedYear = 2026,
            chapters = listOf(
                BookChapter(1, "الفصل الأول: قصة بيت الآحاد وقصر العشرات والقيمة المكانية", "1-30"),
                BookChapter(2, "الفصل الثاني: مكونات العدد 10 واستراتيجيات الجمع الذهني السريع", "31-65"),
                BookChapter(3, "الفصل الثالث: العد القفزي والأنماط الهندسية للأطفال", "66-95"),
                BookChapter(4, "الفصل الرابع: حل المسائل الحياتية بالفهم مش الحفظ 💡", "96-120")
            )
        ),
        LibraryBook(
            id = "book-tot-guide",
            title = "دليل المدرب المعتمد في تدريب الحساب الذهني (TOT)",
            author = "أ / سارة العربي - برنامج العباقرة الصغار",
            category = "إعداد وتأهيل المدربين - العباقرة الصغار",
            pagesCount = 180,
            coverUrl = "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?auto=format&fit=crop&w=600&q=80",
            description = "الدليل المهني للمعلمات والأمهات لاحتراف تدريس السوروبان للأطفال، إدارة قاعة التدريس التفاعلية، وتصميم المسابقات الحماسية.",
            downloadUrl = "https://wa.me/201028340042",
            publishedYear = 2026,
            chapters = listOf(
                BookChapter(1, "الفصل الأول: فلسفة برنامج العباقرة الصغار وتدرج المستويات", "1-45"),
                BookChapter(2, "الفصل الثاني: سيكولوجية الطفل والتغلب على التشتت وصعوبات التعلم", "46-90"),
                BookChapter(3, "الفصل الثالث: تصميم الأنشطة الحركية والألعاب التعليمية داخل الفصل", "91-135"),
                BookChapter(4, "الفصل الرابع: أدوات التقييم وسرعة قياس المعالجة الذهنية للأطفال", "136-180")
            )
        )
    )

    private val initialLiveSessions = listOf(
        LiveSession(
            id = "live-session-1",
            courseId = "course-soroban-kids",
            courseTitle = "دبلوم الحساب الذهني وتنمية الذكاء للأطفال",
            instructorId = "inst-sara-elarby",
            instructorName = "أ / سارة العربي",
            title = "حصة العداد الصيني المباشرة: تدريب تفاعلي على أصدقاء العدد 5 ومشاركة الشاشة",
            description = "بث مباشر تفاعلي مع مس سارة العربي: مشاركة شاشة العداد التفاعلي، حل مسائل جماعية، ورسم الحركات على السبورة البيضاء.",
            scheduledAt = "الآن مباشرة 🔴",
            durationMinutes = 60,
            status = "live",
            attendeesCount = 28,
            meetingCode = "SARA-SOROBAN-LIVE"
        ),
        LiveSession(
            id = "live-session-2",
            courseId = "course-arabic-foundations",
            courseTitle = "تأسيس اللغة العربية والقرائية الممتعة للأطفال",
            instructorId = "inst-sara-elarby",
            instructorName = "أ / سارة العربي",
            title = "ورشة القراءة الصوتية للأمهات والمعلمات: كيف نعلّم الطفل التحليل الصوتي بالفهم",
            description = "جلسة تدريبية مباشرة مع مس سارة تركز على مخارج الحروف، التمييز بين المدود والحركات، ودمج المقاطع الصوتية بدون تشتت.",
            scheduledAt = "غداً 06:00 م بتوقيت القاهرة",
            durationMinutes = 75,
            status = "upcoming",
            attendeesCount = 45,
            meetingCode = "SARA-NOUR-BAYAN"
        ),
        LiveSession(
            id = "live-session-3",
            courseId = "course-tot-trainers",
            courseTitle = "دبلوم تدريب وإعداد مدربي الحساب الذهني المعتمدين (TOT)",
            instructorId = "inst-sara-elarby",
            instructorName = "أ / سارة العربي",
            title = "لقاء مدربي العباقرة الصغار: استراتيجيات تحفيز الطفل وتصميم أنشطة الحصة",
            description = "مناقشة حية لخطط التدريب، حل مشكلات تشتت انتباه الأطفال، ومحاكاة عملية لشرح قواعد الـ Big Friends.",
            scheduledAt = "الجمعة القادمة 07:00 م",
            durationMinutes = 90,
            status = "upcoming",
            attendeesCount = 36,
            meetingCode = "SARA-TOT-GENIUS"
        )
    )

    private val initialAssignments = listOf(
        Assignment(
            id = "asg-soroban-practice",
            courseId = "course-soroban-kids",
            courseTitle = "دبلوم الحساب الذهني وتنمية الذكاء للأطفال",
            title = "تحدي العباقرة الصغار: حل 20 مسألة على العداد الصيني مع تسجيل فيديو دقيقة",
            description = "يقوم البطل بحل ورقة التمارين المرفقة باستخدام العداد الصيني (أو الحساب التخيلي)، مع كتابة النواتج وتصوير مقطع فيديو قصير لحركة الأصابع.",
            rubric = listOf(
                "استخدام الإبهام والسبابة بالحركة الصحيحة (30 درجة)",
                "صحة النواتج الرياضية (40 درجة)",
                "السرعة والثقة أثناء التحريك (30 درجة)"
            ),
            dueDate = "2026-11-10",
            maxScore = 100
        ),
        Assignment(
            id = "asg-arabic-analysis",
            courseId = "course-arabic-foundations",
            courseTitle = "تأسيس اللغة العربية والقرائية الممتعة للأطفال",
            title = "واجب التحليل الصوتي: تحليل 10 كلمات إلى مقاطع صوتية بالفتح والكسر",
            description = "قراءة الكلمات بصوت واضح، ثم تفكيك كل كلمة إلى مقاطعها الصوتية وفق قواعد نور البيان وتوضيح المد والممدود.",
            rubric = listOf(
                "التهجي الصحيح ومخارج الأصوات (40 درجة)",
                "التحليل الصوتي للمقاطع (40 درجة)",
                "جمال ونظافة خط الكتابة (20 درجة)"
            ),
            dueDate = "2026-11-15",
            maxScore = 100
        )
    )

    private val initialSubmissions = mutableListOf(
        StudentSubmission(
            id = "sub-01",
            assignmentId = "asg-soroban-practice",
            assignmentTitle = "تحدي العباقرة الصغار: حل 20 مسألة على العداد الصيني",
            studentId = "usr-student-1",
            studentName = "ياسين أحمد (بطل العباقرة)",
            submittedAt = "2026-10-28 18:30",
            textSubmission = "حليت كل المسائل يا مس سارة بالعداد الصيني وبالتخيل الذهني، وطلعت النواتج في 3 دقائق بس! شكراً ليكي يا أحسن معلمة 🌟💡",
            repoUrl = "https://wa.me/201028340042",
            grade = 98,
            maxScore = 100,
            feedback = "ما شاء الله عليك يا ياسين يا بطل! حركة أصابعك سريعة ودقيقة جداً، وتخيلك للخرزات رائع.. استمر في التدريب اليومي 💡👏",
            status = "graded"
        )
    )

    private val initialCertificates = mutableListOf(
        Certificate(
            id = "cert-01",
            enrollmentId = "enr-soroban-1",
            courseId = "course-soroban-kids",
            courseTitle = "دبلوم الحساب الذهني وتنمية الذكاء للأطفال",
            userId = "usr-student-1",
            userName = "ياسين أحمد",
            instructorName = "أ / سارة العربي",
            verificationCode = "CERT-2026-SOROBAN-001",
            issuedAt = "2026-10-25",
            gradePercentage = 98,
            skillsVerified = listOf("الجمع والطرح المباشر", "أصدقاء العدد 5", "الحساب التخيلي Anzan", "السوروبان السريع")
        )
    )

    private val initialEnrollments = mutableListOf(
        Enrollment(
            id = "enr-soroban-1",
            userId = "usr-student-1",
            courseId = "course-soroban-kids",
            status = "active",
            source = "purchase",
            enrolledAt = "2026-10-15",
            currentLessonId = "les-soroban-1-2"
        ),
        Enrollment(
            id = "enr-ar-1",
            userId = "usr-student-1",
            courseId = "course-arabic-foundations",
            status = "active",
            source = "free",
            enrolledAt = "2026-10-20",
            currentLessonId = "les-ar-1"
        )
    )

    private val initialProgress = mutableMapOf(
        "les-soroban-1-1" to LessonProgress(
            userId = "usr-student-1",
            lessonId = "les-soroban-1-1",
            progressSeconds = 420,
            completed = true,
            completedAt = "2026-10-18"
        )
    )

    private val initialAuditLogs = mutableListOf(
        AuditEvent(
            id = "aud-01",
            actorUserId = "usr-admin-1",
            actorName = "م. عمر المنصوري",
            action = "COURSE_PUBLISHED",
            resourceType = "course",
            resourceId = "course-soroban-kids",
            reason = "اعتماد ونشر دبلوم الحساب الذهني لعام 2026",
            createdAt = "2026-01-15 10:00"
        ),
        AuditEvent(
            id = "aud-02",
            actorUserId = "inst-sara-elarby",
            actorName = "أ / سارة العربي",
            action = "CERTIFICATE_ISSUED",
            resourceType = "certificate",
            resourceId = "cert-01",
            reason = "إصدار شهادة اجتياز للطالب ياسين أحمد بدرجة 98%",
            correlationId = "CERT-2026-SOROBAN-001",
            createdAt = "2026-10-25 15:40"
        ),
        AuditEvent(
            id = "aud-03",
            actorUserId = "inst-sara-elarby",
            actorName = "أ / سارة العربي",
            action = "SUBMISSION_GRADED",
            resourceType = "assignment",
            resourceId = "sub-01",
            reason = "تصحيح تحدي العداد الصيني للطالب ياسين أحمد وإضافة الملاحظات",
            createdAt = "2026-10-28 19:15"
        )
    )

    private val initialClassroomMessages = mutableListOf(
        ClassroomMessage("msg-1", "inst-sara-elarby", "أ / سارة العربي", "instructor", "أهلاً يا أبطال في البث المباشر للعداد الصيني! مين جهّز العداد بتاعه؟ 🧮💡", "18:00"),
        ClassroomMessage("msg-2", "usr-student-1", "ياسين أحمد", "learner", "أنا جهزت العداد يا مس سارة ومستعد للتحدي! 🌟", "18:02"),
        ClassroomMessage("msg-3", "usr-student-2", "نور الهدى", "learner", "يا مس إمتى بنستخدم صديق الـ 5 مع رقم 3؟", "18:04", isQuestion = true, upvotes = 4)
    )

    // Reactive State Holders
    private val _courses = MutableStateFlow(initialCourses)
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _books = MutableStateFlow(initialBooks)
    val books: StateFlow<List<LibraryBook>> = _books.asStateFlow()

    private val _liveSessions = MutableStateFlow(initialLiveSessions)
    val liveSessions: StateFlow<List<LiveSession>> = _liveSessions.asStateFlow()

    private val _assignments = MutableStateFlow(initialAssignments)
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    private val _submissions = MutableStateFlow<List<StudentSubmission>>(initialSubmissions)
    val submissions: StateFlow<List<StudentSubmission>> = _submissions.asStateFlow()

    private val _certificates = MutableStateFlow<List<Certificate>>(initialCertificates)
    val certificates: StateFlow<List<Certificate>> = _certificates.asStateFlow()

    private val _enrollments = MutableStateFlow<List<Enrollment>>(initialEnrollments)
    val enrollments: StateFlow<List<Enrollment>> = _enrollments.asStateFlow()

    private val _progressMap = MutableStateFlow<Map<String, LessonProgress>>(initialProgress)
    val progressMap: StateFlow<Map<String, LessonProgress>> = _progressMap.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditEvent>>(initialAuditLogs)
    val auditLogs: StateFlow<List<AuditEvent>> = _auditLogs.asStateFlow()

    private val _classroomMessages = MutableStateFlow<List<ClassroomMessage>>(initialClassroomMessages)
    val classroomMessages: StateFlow<List<ClassroomMessage>> = _classroomMessages.asStateFlow()

    private val _consultationBookings = MutableStateFlow<List<ConsultationBooking>>(emptyList())
    val consultationBookings: StateFlow<List<ConsultationBooking>> = _consultationBookings.asStateFlow()

    /**
     * Persists a real consultation booking request submitted from the booking
     * funnel, records an audit trail event, and returns the stored booking so
     * callers can display its reference id. Surfaced to instructor/admin portals.
     */
    fun bookConsultation(
        parentName: String,
        childName: String,
        childAge: String,
        phone: String,
        service: String,
        actorUserId: String = "guest",
        actorName: String = "زائر"
    ): ConsultationBooking {
        val booking = ConsultationBooking(
            id = "bkg-" + System.currentTimeMillis(),
            parentName = parentName.trim(),
            childName = childName.trim(),
            childAge = childAge.trim(),
            phone = phone.trim(),
            service = service,
            status = "PENDING",
            createdAt = OrbitTimeFormatter.currentDateTime()
        )
        _consultationBookings.value = listOf(booking) + _consultationBookings.value
        addAudit(
            actorUserId = actorUserId,
            actorName = actorName,
            action = "CONSULTATION_BOOKED",
            resourceType = "booking",
            resourceId = booking.id,
            reason = "ولي أمر: ${booking.parentName} | طفل: ${booking.childName} (${booking.childAge}) | هاتف: ${booking.phone} | برنامج: ${booking.service}",
            correlationId = booking.id
        )
        return booking
    }

    /** Instructor-side workflow: advance a booking's status with an audit record. */
    fun updateBookingStatus(bookingId: String, newStatus: String, actorName: String) {
        if (_consultationBookings.value.none { it.id == bookingId }) return
        _consultationBookings.value = _consultationBookings.value.map {
            if (it.id == bookingId) it.copy(status = newStatus) else it
        }
        addAudit(
            actorUserId = "inst-sara-elarby",
            actorName = actorName,
            action = "BOOKING_STATUS_$newStatus",
            resourceType = "booking",
            resourceId = bookingId,
            correlationId = bookingId
        )
    }

    // Helper functions
    fun enrollCourse(userId: String, courseId: String, source: String): Enrollment {
        val existing = _enrollments.value.find { it.userId == userId && it.courseId == courseId }
        if (existing != null) return existing

        val newEnrollment = Enrollment(
            id = "enr-" + System.currentTimeMillis(),
            userId = userId,
            courseId = courseId,
            status = "active",
            source = source,
            enrolledAt = OrbitTimeFormatter.currentDate()
        )
        _enrollments.value = listOf(newEnrollment) + _enrollments.value

        addAudit(
            actorUserId = userId,
            actorName = getUserName(userId),
            action = "ENROLLMENT_CREATED",
            resourceType = "course",
            resourceId = courseId,
            reason = "تسجيل فوري في الدورة"
        )
        return newEnrollment
    }

    fun completeLesson(userId: String, lessonId: String, courseId: String) {
        val now = OrbitTimeFormatter.currentDateTime()
        val updated = _progressMap.value.toMutableMap()
        updated[lessonId] = LessonProgress(
            userId = userId,
            lessonId = lessonId,
            progressSeconds = 600,
            completed = true,
            completedAt = now
        )
        _progressMap.value = updated
    }

    fun issueCertificate(courseId: String, courseTitle: String, userId: String, userName: String, grade: Int, skills: List<String>): Certificate {
        val existing = _certificates.value.find { it.courseId == courseId && it.userId == userId }
        if (existing != null) return existing

        val code = "CERT-2026-" + courseId.takeLast(6).uppercase(Locale.ROOT) + "-" + (100..999).random()
        val cert = Certificate(
            id = "cert-" + System.currentTimeMillis(),
            enrollmentId = "enr-" + System.currentTimeMillis(),
            courseId = courseId,
            courseTitle = courseTitle,
            userId = userId,
            userName = userName,
            instructorName = INSTRUCTOR_SARA.name,
            verificationCode = code,
            issuedAt = OrbitTimeFormatter.currentDate(),
            gradePercentage = grade,
            skillsVerified = skills
        )
        _certificates.value = listOf(cert) + _certificates.value

        addAudit(
            actorUserId = "inst-sara-elarby",
            actorName = INSTRUCTOR_SARA.name,
            action = "CERTIFICATE_ISSUED",
            resourceType = "certificate",
            resourceId = cert.id,
            reason = "إصدار شهادة اجتياز معتمدة للطالب $userName بكود $code",
            correlationId = code
        )
        return cert
    }

    fun submitAssignment(assignmentId: String, assignmentTitle: String, studentId: String, studentName: String, text: String, repoUrl: String?): StudentSubmission {
        val sub = StudentSubmission(
            id = "sub-" + System.currentTimeMillis(),
            assignmentId = assignmentId,
            assignmentTitle = assignmentTitle,
            studentId = studentId,
            studentName = studentName,
            submittedAt = OrbitTimeFormatter.currentDateTime(),
            textSubmission = text,
            repoUrl = repoUrl,
            status = "submitted"
        )
        _submissions.value = listOf(sub) + _submissions.value

        addAudit(
            actorUserId = studentId,
            actorName = studentName,
            action = "ASSIGNMENT_SUBMITTED",
            resourceType = "assignment",
            resourceId = assignmentId,
            reason = "تسليم واجب الطالب: $assignmentTitle"
        )
        return sub
    }

    fun gradeSubmission(subId: String, grade: Int, feedback: String, graderName: String) {
        _submissions.value = _submissions.value.map {
            if (it.id == subId) {
                it.copy(grade = grade, feedback = feedback, status = "graded")
            } else it
        }

        addAudit(
            actorUserId = "inst-sara-elarby",
            actorName = graderName,
            action = "SUBMISSION_GRADED",
            resourceType = "assignment_submission",
            resourceId = subId,
            reason = "رصد الدرجة $grade/100 مع الملاحظات"
        )
    }

    fun sendClassroomMessage(senderId: String, senderName: String, role: String, text: String, isQuestion: Boolean = false) {
        val time = OrbitTimeFormatter.currentTime()
        val msg = ClassroomMessage(
            id = "msg-" + System.currentTimeMillis(),
            senderId = senderId,
            senderName = senderName,
            senderRole = role,
            text = text,
            timestamp = time,
            isQuestion = isQuestion
        )
        _classroomMessages.value = _classroomMessages.value + listOf(msg)
    }

    fun upvoteQuestion(messageId: String) {
        _classroomMessages.value = _classroomMessages.value.map {
            if (it.id == messageId) it.copy(upvotes = it.upvotes + 1) else it
        }
    }

    fun addAudit(actorUserId: String, actorName: String, action: String, resourceType: String, resourceId: String, reason: String = "", correlationId: String = "") {
        val event = AuditEvent(
            id = "aud-" + System.currentTimeMillis(),
            actorUserId = actorUserId,
            actorName = actorName,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            reason = reason,
            correlationId = correlationId,
            createdAt = OrbitTimeFormatter.currentDateTime()
        )
        _auditLogs.value = listOf(event) + _auditLogs.value
    }

    fun verifyCertificate(code: String): Certificate? {
        val clean = code.trim().uppercase(Locale.ROOT)
        return _certificates.value.find { it.verificationCode.equals(clean, ignoreCase = true) }
    }

    private fun getUserName(userId: String): String {
        return PRESET_USERS.find { it.id == userId }?.displayName ?: "مستخدم"
    }
}
