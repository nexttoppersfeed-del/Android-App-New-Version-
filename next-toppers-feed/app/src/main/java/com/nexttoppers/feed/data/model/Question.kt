package com.nexttoppers.feed.data.model

data class Question(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),   // exactly 4 options (A-D)
    val correctAnswer: Int = 0,                // 0-based index into options
    val explanation: String = "",
    val imageUrl: String = ""
) {
    fun optionLetter(index: Int) = listOf("A", "B", "C", "D").getOrElse(index) { "?" }
    fun isCorrect(selectedIndex: Int) = selectedIndex == correctAnswer
}
