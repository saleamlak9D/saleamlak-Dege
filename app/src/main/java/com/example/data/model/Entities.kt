package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val duration: String,
    val difficulty: String,
    val content: String,
    val managerName: String = "Salish Manager",
    val isDownloaded: Boolean = true,
    val bannerUrl: String = ""
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val courseTitle: String,
    val title: String,
    val durationMinutes: Int = 15,
    val totalQuestions: Int = 0
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String // "A", "B", "C", "D"
)

@Entity(tableName = "results")
data class ExamResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val examTitle: String,
    val courseTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_classes")
data class LiveClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val instructor: String = "Instructor Salish",
    val scheduledTime: String, // format e.g., "Today, 7:00 PM"
    val durationMinutes: Int = 45,
    val isLiveNow: Boolean = false,
    val chatMessagesJson: String = "[]" // JSON array of live messages
)

@Entity(tableName = "chatbot_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "model"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class PushNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
