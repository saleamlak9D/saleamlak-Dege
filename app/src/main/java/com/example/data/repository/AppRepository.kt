package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    val allCourses: Flow<List<Course>> = appDao.getAllCoursesFlow()
    val allExams: Flow<List<Exam>> = appDao.getAllExamsFlow()
    val allResults: Flow<List<ExamResult>> = appDao.getAllResultsFlow()
    val allLiveClasses: Flow<List<LiveClass>> = appDao.getAllLiveClassesFlow()
    val chatMessages: Flow<List<ChatMessage>> = appDao.getChatMessagesFlow()
    val notifications: Flow<List<PushNotification>> = appDao.getAllNotificationsFlow()

    suspend fun insertCourse(course: Course): Long = appDao.insertCourse(course)

    suspend fun insertExam(exam: Exam): Long = appDao.insertExam(exam)

    suspend fun insertQuestions(questions: List<Question>) = appDao.insertQuestions(questions)

    suspend fun getQuestionsForExam(examId: Long): List<Question> = appDao.getQuestionsForExam(examId)

    suspend fun insertResult(result: ExamResult) = appDao.insertResult(result)

    suspend fun insertLiveClass(liveClass: LiveClass) = appDao.insertLiveClass(liveClass)

    suspend fun updateLiveClass(liveClass: LiveClass) = appDao.updateLiveClass(liveClass)

    suspend fun insertChatMessage(message: ChatMessage) = appDao.insertChatMessage(message)

    suspend fun clearChatMessages() = appDao.clearChatMessages()

    suspend fun insertNotification(notification: PushNotification) = appDao.insertNotification(notification)

    suspend fun markNotificationAsRead(id: Long) = appDao.markNotificationAsRead(id)

    suspend fun askGemini(prompt: String, conversationHistory: List<ChatMessage>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Hello! I am Salish AI. Currently, your GEMINI_API_KEY is not set. Please add it via the Secrets panel in AI Studio to enable fully dynamic AI tutoring."
        }

        // Map conversation history to Gemini API format, mapping chat message roles:
        // Room ChatMessage uses "user" and "model" roles
        val contentsList = conversationHistory.map { msg ->
            val roleName = if (msg.role == "user") "user" else "model"
            // Note: Direct REST api generateContent contents format can have 'role' but let's send contents list simply:
            // contents array: [{"role": "user", "parts": [{"text": "..."}]}, ...]
            // Let's create a custom Content data class representation in our api to support roles,
            // or just send simple sequential texts if we want to keep it simple.
            // Let's map role to our api Content structure:
            // Wait, does our api Content have a role?
            // In GeminiApiService, we defined: data class Content(val parts: List<Part>)
            // Wait, we can edit GeminiApiService to support role or just send parts as a list.
            // Sending sequential messages in contents is fully supported.
            Content(parts = listOf(Part(text = msg.messageText)))
        } + Content(parts = listOf(Part(text = prompt)))

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = "You are Salish AI, an intelligent educational tutor assistant for Salish eLearning. " +
                            "Help the user with queries about course content, exams, live classes, study tips, or general academic advice. " +
                            "Be professional, direct, encouraging, and clear. Keep response lengths below 3 sentences unless specifically asked for details."
                )
            )
        )

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = systemInstruction
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I received an empty response. Please try again."
        } catch (e: Exception) {
            "Offline Mode: I couldn't reach the server. Make sure your internet connection is active and your API key is correct. (Reason: ${e.localizedMessage})"
        }
    }
}
