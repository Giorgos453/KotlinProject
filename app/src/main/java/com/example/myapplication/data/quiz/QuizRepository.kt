package com.example.myapplication.data.quiz

import android.content.Context
import com.example.myapplication.util.AppLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for quiz questions – wraps DAO access and seed logic.
 */
class QuizRepository(
    private val dao: QuizQuestionDao,
    private val context: Context
) {
    private val tag = "QuizRepository"

    val allQuestions: Flow<List<QuizQuestion>> = dao.getAllQuestions()

    fun getQuestionsByCategory(category: String): Flow<List<QuizQuestion>> =
        dao.getQuestionsByCategory(category)

    suspend fun getRandomQuestions(limit: Int): List<QuizQuestion> =
        dao.getRandomQuestions(limit)

    /**
     * Loads quiz questions from assets/quiz_questions.json and inserts them into the DB.
     * Only runs if the table is still empty.
     */
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count > 0) {
            AppLogger.d(tag, "Quiz questions already present ($count entries), skipping seed")
            return@withContext
        }

        try {
            val json = context.assets.open("quiz_questions.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<QuizQuestion>>() {}.type
            val questions: List<QuizQuestion> = Gson().fromJson(json, type)

            dao.insertAll(questions)
            AppLogger.i(tag, "Successfully seeded ${questions.size} quiz questions")
        } catch (e: Exception) {
            AppLogger.e(tag, "Error seeding quiz questions", e)
        }
    }
}
