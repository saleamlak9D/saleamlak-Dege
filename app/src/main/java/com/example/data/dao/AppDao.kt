package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Courses
    @Query("SELECT * FROM courses ORDER BY id DESC")
    fun getAllCoursesFlow(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    // Exams
    @Query("SELECT * FROM exams ORDER BY id DESC")
    fun getAllExamsFlow(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE courseId = :courseId")
    suspend fun getExamsForCourse(courseId: Long): List<Exam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    // Questions
    @Query("SELECT * FROM questions WHERE examId = :examId")
    suspend fun getQuestionsForExam(examId: Long): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    // Results
    @Query("SELECT * FROM results ORDER BY timestamp DESC")
    fun getAllResultsFlow(): Flow<List<ExamResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ExamResult)

    // Live Classes
    @Query("SELECT * FROM live_classes ORDER BY id DESC")
    fun getAllLiveClassesFlow(): Flow<List<LiveClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveClass(liveClass: LiveClass)

    @Update
    suspend fun updateLiveClass(liveClass: LiveClass)

    // Chatbot Messages
    @Query("SELECT * FROM chatbot_messages ORDER BY timestamp ASC")
    fun getChatMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chatbot_messages")
    suspend fun clearChatMessages()

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<PushNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: PushNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)
}
