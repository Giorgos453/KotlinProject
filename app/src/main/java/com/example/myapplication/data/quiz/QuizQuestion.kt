package com.example.myapplication.data.quiz

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for UPM quiz questions.
 */
@Entity(tableName = "quiz_questions")
data class QuizQuestion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "option1") val option1: String,
    @ColumnInfo(name = "option2") val option2: String,
    @ColumnInfo(name = "option3") val option3: String,
    @ColumnInfo(name = "option4") val option4: String,
    @ColumnInfo(name = "correct_index") val correctIndex: Int,
    @ColumnInfo(name = "category") val category: String
)
