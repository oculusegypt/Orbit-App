package com.example.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.core.OrbitTimeFormatter

/**
 * Owns the Socratic AI Tutor conversation state and reply generation.
 * Extracted from [OrbitViewModel] to isolate tutor-specific logic.
 */
class SocraticTutorController {

    private val _messages = MutableStateFlow<List<TutorMessage>>(listOf(welcomeMessage()))
    val messages: StateFlow<List<TutorMessage>> = _messages.asStateFlow()

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun setInput(text: String) {
        _input.value = text
    }

    /**
     * Sends [queryText] (or the current input when null) as a user message and,
     * after a short "thinking" delay, appends a rule-based Socratic reply that is
     * contextualized with the currently open lesson title ([lessonTitleProvider]).
     */
    fun sendQuery(scope: CoroutineScope, queryText: String? = null, lessonTitleProvider: () -> String) {
        val query = (queryText ?: _input.value).trim()
        if (query.isBlank()) return

        _messages.value += TutorMessage(id = nextId("usr"), role = "user", content = query)
        _input.value = ""
        _loading.value = true

        scope.launch {
            delay(THINKING_DELAY_MS) // Socratic thinking animation
            val lessonTitle = lessonTitleProvider()
            _messages.value += TutorMessage(
                id = nextId("tutor"),
                role = "assistant",
                content = buildReply(query),
                citations = listOf(TutorCitation(lessonTitle, REFERENCE_LABEL))
            )
            _loading.value = false
        }
    }

    private fun buildReply(query: String): String = when {
        query.contains("أصدقاء") || query.contains("5") ->
            "أحسنت السؤال يا بطل! 💡\nفي قاعدة أصدقاء العدد 5، لو عندنا 4 وعاوزين نضيف 1، إيه أقرب رقم صديق للـ 5 نقدر نستخدمه؟\nالمفتاح السقراطي: الخرزة العلوية بتساوي 5، يبقى بننزل الـ 5 ونطرح كام؟ شاطر، بنطرح 4! (+1 = +5 - 4). جربها بإيدك على العداد وشوف!"
        query.contains("خرزة") || query.contains("خرزات") ->
            "سؤال ذكي جداً! 🧮\nافتكر دايماً: خرزة السماء العلوية فوق العارضة بتساوي 5، والأربع خرزات الأرضيات تحت كل واحدة بتساوي 1.\nأي خرزة بتلمس العارضة الفاصلة، بيتم حسابها فوراً. جرب تحرك خرزة السماء مع خرزتين تحت وشوف الناتج هيكون 7!"
        query.contains("تلميح") || query.contains("حل") ->
            "أنا معاك خطوة بخطوة! 🌟\nالتلميح الذهبي لمس سارة: اتخيل العداد قدامك وافتكر حركة الأصابع.. الجمع بإصبع الإبهام نحو العارضة، والطرح بالسبابة! جرب تحسبها الأول بعينك وبعدين بإيدك."
        query.contains("لغز") || query.contains("سؤال") ->
            "جاهز للتحدي يا عبقري؟ 🎯\nلو عندك 3 خرزات لامسين العارضة، ونزلت خرزة السماء (5)، وبعدين طرحت 2.. الناتج النهائي في عقلك هيكون كام؟ فكر وقولي!"
        else ->
            "فكرة ممتازة وتفكير رائع يا بطل! 💡\nفي منهج مدار لتأسيس الأطفال والحساب الذهني، السر كله في 'الفهم مش الحفظ'.. اتخيل حركة الخرزات السحرية في عقلك وخلّي أصابعك خفيفة وسريعة. إيه الجزء اللي حابب نجربه سوا دلوقتي؟"
    }

    private fun welcomeMessage() = TutorMessage(
        id = "msg-welcome",
        role = "assistant",
        content = "أهلاً بك يا بطل! 🌟\nأنا المساعد السقراطي الذكي لمس سارة العربي 💡\nهنا بنتعلم سوا بطريقة ممتعة: \"تأسيس أطفال . حساب ذهني\"! لو في خطوة أو مسألة مش واضحة، اسألني وهفكر معاك ونديك تلميحات حلوة توصلك للحل بنفسك! 🧠✨"
    )

    private fun nextId(prefix: String): String = "$prefix-${System.currentTimeMillis()}"

    private companion object {
        const val THINKING_DELAY_MS = 800L
        const val REFERENCE_LABEL = "مرجع من منهج مس سارة العربي 2026"
    }
}
