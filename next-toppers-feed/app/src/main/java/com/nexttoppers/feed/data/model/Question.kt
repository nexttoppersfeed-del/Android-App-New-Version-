package com.nexttoppers.feed.data.model

// F22: matches website's tests.questions array element schema
data class Question(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    // F22: website stores correctAnswer as string ("A"/"B"/"C"/"D" or the answer text)
    // correctAnswerIndex is the derived 0-based index for UI use
    val correctAnswer: String = "",
    val explanation: String = "",
    val subject: String = "",
    val marks: Int = 1,
    val imageUrl: String = ""
) {
    // Derive 0-based index from the stored string answer
    val correctAnswerIndex: Int
        get() {
            if (correctAnswer.isBlank()) return 0
            return when (correctAnswer.uppercase().trim()) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                "D" -> 3
                else -> options.indexOfFirst { it.equals(correctAnswer, ignoreCase = true) }
                            .coerceAtLeast(0)
            }
        }

    fun optionLetter(index: Int) = listOf("A", "B", "C", "D").getOrElse(index) { "?" }
    fun isCorrect(selectedIndex: Int) = selectedIndex == correctAnswerIndex
}

// Helper: map a raw Firestore question map to a Question object
fun mapRawQuestion(raw: Map<String, Any>, index: Int): Question {
    return Question(
        id            = raw["id"] as? String ?: "q$index",
        question      = raw["question"] as? String ?: "",
        options       = (raw["options"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        correctAnswer = raw["correctAnswer"] as? String ?: "",
        explanation   = raw["explanation"] as? String ?: "",
        subject       = raw["subject"] as? String ?: "",
        marks         = ((raw["marks"] ?: 1L) as? Long)?.toInt() ?: 1,
        imageUrl      = raw["imageUrl"] as? String ?: ""
    )
}
