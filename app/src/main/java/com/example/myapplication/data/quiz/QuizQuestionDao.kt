package com.example.myapplication.data.quiz

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for quiz questions – read and seed operations.
 */
@Dao
interface QuizQuestionDao {

    @Query("SELECT * FROM quiz_questions")
    fun getAllQuestions(): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE category = :category")
    fun getQuestionsByCategory(category: String): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int): List<QuizQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuizQuestion>)

    @Query("SELECT COUNT(*) FROM quiz_questions")
    suspend fun getCount(): Int
}
