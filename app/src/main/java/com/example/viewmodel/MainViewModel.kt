package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserRole { STUDENT, MANAGER }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao())

    // App Roles
    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // App Connectivity and Caching State
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncProgress = MutableStateFlow(-1f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _syncPhase = MutableStateFlow("Idle")
    val syncPhase: StateFlow<String> = _syncPhase.asStateFlow()

    // Core Data flows
    val courses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val results: StateFlow<List<ExamResult>> = repository.allResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveClasses: StateFlow<List<LiveClass>> = repository.allLiveClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<PushNotification>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI state
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    private val _selectedLiveClass = MutableStateFlow<LiveClass?>(null)
    val selectedLiveClass: StateFlow<LiveClass?> = _selectedLiveClass.asStateFlow()

    // Chatbot loading state
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Active exam taking state
    private val _activeExam = MutableStateFlow<Exam?>(null)
    val activeExam: StateFlow<Exam?> = _activeExam.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<Question>>(emptyList())
    val activeQuestions: StateFlow<List<Question>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // Question ID to Option index string (e.g. "A", "B", "C", "D")
    private val _selectedAnswers = MutableStateFlow<Map<Long, String>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Long, String>> = _selectedAnswers.asStateFlow()

    private val _examFinishedResult = MutableStateFlow<ExamResult?>(null)
    val examFinishedResult: StateFlow<ExamResult?> = _examFinishedResult.asStateFlow()

    // Notification triggered status
    private val _lastSimulatedNotification = MutableStateFlow<PushNotification?>(null)
    val lastSimulatedNotification: StateFlow<PushNotification?> = _lastSimulatedNotification.asStateFlow()

    init {
        // Prepopulate empty database with rich content
        viewModelScope.launch {
            courses.first { it.isNotEmpty() || true } // wait flow
            delay(500)
            if (courses.value.isEmpty()) {
                prepopulateDatabase()
            }
            // Start background simulation for dynamic notifications and announcements
            startSimulationEngine()
        }
    }

    fun toggleRole() {
        _currentRole.value = if (_currentRole.value == UserRole.STUDENT) UserRole.MANAGER else UserRole.STUDENT
    }

    fun toggleNetworkStatus() {
        _isOnline.value = !_isOnline.value
        val statusStr = if (_isOnline.value) "Online (Sync Active)" else "Offline Mode (Running from SQLite Local Cache)"
        viewModelScope.launch {
            val notification = PushNotification(
                title = "Network Connection Status Change",
                body = "System transitioned to $statusStr."
            )
            repository.insertNotification(notification)
            _lastSimulatedNotification.value = notification
        }
    }

    fun performSyncCache() {
        viewModelScope.launch {
            _syncProgress.value = 0.0f
            _syncPhase.value = "Initiating handshake..."
            delay(600)
            _syncProgress.value = 0.25f
            _syncPhase.value = "Downloading course syllabus & syllabus media..."
            delay(800)
            _syncProgress.value = 0.50f
            _syncPhase.value = "Caching assessment questions into local storage..."
            delay(800)
            _syncProgress.value = 0.75f
            _syncPhase.value = "Syncing chatbot offline logs & integrity validation..."
            delay(600)
            _syncProgress.value = 1.0f
            _syncPhase.value = "Completed! SQLite database fully updated."
            delay(500)
            _syncProgress.value = -1f
            _syncPhase.value = "Idle"

            val notification = PushNotification(
                title = "Database Cache Restored!",
                body = "Successfully cached all curriculum materials and exam configurations into secure local storage."
            )
            repository.insertNotification(notification)
            _lastSimulatedNotification.value = notification
        }
    }

    fun selectCourse(course: Course?) {
        _selectedCourse.value = course
    }

    fun selectLiveClass(liveClass: LiveClass?) {
        _selectedLiveClass.value = liveClass
    }

    fun clearFinishedResult() {
        _examFinishedResult.value = null
    }

    // Exam functions
    fun startExam(exam: Exam) {
        viewModelScope.launch {
            _activeExam.value = exam
            val qList = repository.getQuestionsForExam(exam.id)
            _activeQuestions.value = qList
            _currentQuestionIndex.value = 0
            _selectedAnswers.value = emptyMap()
            _examFinishedResult.value = null
        }
    }

    fun selectAnswer(questionId: Long, answerOption: String) {
        _selectedAnswers.value = _selectedAnswers.value + (questionId to answerOption)
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _activeQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun submitExam() {
        val exam = _activeExam.value ?: return
        val questions = _activeQuestions.value
        if (questions.isEmpty()) return

        var score = 0
        questions.forEach { q ->
            val userAns = _selectedAnswers.value[q.id]
            if (userAns == q.correctOption) {
                score++
            }
        }

        val pct = (score.toFloat() / questions.size.toFloat()) * 100f
        val result = ExamResult(
            examId = exam.id,
            examTitle = exam.title,
            courseTitle = exam.courseTitle,
            score = score,
            totalQuestions = questions.size,
            percentage = pct
        )

        viewModelScope.launch {
            repository.insertResult(result)
            _examFinishedResult.value = result

            // Trigger Real-time simulated push notification
            val notification = PushNotification(
                title = "Exam Complete!",
                body = "You scored $score/${questions.size} ($pct%) on your primary exam: ${exam.title}."
            )
            repository.insertNotification(notification)
            _lastSimulatedNotification.value = notification

            // Clear active exam states
            _activeExam.value = null
            _activeQuestions.value = emptyList()
        }
    }

    fun cancelExam() {
        _activeExam.value = null
        _activeQuestions.value = emptyList()
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
    }

    // Chatbot function
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // 1. Add user message
            val userMsg = ChatMessage(role = "user", messageText = text)
            repository.insertChatMessage(userMsg)

            _isChatLoading.value = true

            // 2. Fetch context
            val history = chatMessages.value

            // 3. Query Gemini or use Offline cached AI responder
            val reply = if (_isOnline.value) {
                repository.askGemini(text, history)
            } else {
                delay(1200) // simulate local processing
                val lower = text.lowercase()
                when {
                    lower.contains("compose") || lower.contains("ui") -> {
                        "Offline Tutor Response: Jetpack Compose is a declarative UI toolkit. You define composables with the @Composable annotation, and use standard layout functions like Column, Row, and Box to align components vertically, horizontally, or stacked. State is preserved across recompositions using remember { mutableStateOf(...) }."
                    }
                    lower.contains("kotlin") || lower.contains("safety") -> {
                        "Offline Tutor Response: Kotlin guarantees safety against NullPointerExceptions by making all variable types non-nullable by default. If a variable is allowed to hold null, its type must be explicitly declared with a question mark (e.g., String?)."
                    }
                    lower.contains("exam") || lower.contains("question") || lower.contains("test") -> {
                        "Offline Tutor Response: All course exams are cached locally on your device in our secure SQLite storage! You can start and complete these assessments entirely offline. Once connection is restored, your results will be synced with the main portal."
                    }
                    else -> {
                        "Offline Tutor Response: I am currently answering from my pre-cached offline knowledge base because you are in Offline Mode. To ask me dynamic real-time questions, please reconnect using the network toggle in your Profile tab."
                    }
                }
            }

            // 4. Add model message
            val modelMsg = ChatMessage(role = "model", messageText = reply)
            repository.insertChatMessage(modelMsg)

            _isChatLoading.value = false

            // Trigger a simulated notification for a chatbot response to show engagement
            val notification = PushNotification(
                title = "Salish AI Tutor",
                body = "Replied: \"${if (reply.length > 50) reply.take(47) + "..." else reply}\""
            )
            repository.insertNotification(notification)
            _lastSimulatedNotification.value = notification
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatMessages()
        }
    }

    // Notification read
    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearLastSimulatedNotification() {
        _lastSimulatedNotification.value = null
    }

    // Manager functions
    fun uploadCourse(
        title: String,
        description: String,
        category: String,
        duration: String,
        difficulty: String,
        content: String,
        bannerUrl: String = ""
    ) {
        viewModelScope.launch {
            val courseId = repository.insertCourse(
                Course(
                    title = title,
                    description = description,
                    category = category,
                    duration = duration,
                    difficulty = difficulty,
                    content = content,
                    bannerUrl = bannerUrl
                )
            )

            // Auto-trigger push notification for all students
            val notification = PushNotification(
                title = "New Course Uploaded!",
                body = "Manager has published: \"$title\" ($difficulty). Enroll today and expand your knowledge offline!"
            )
            repository.insertNotification(notification)
            _lastSimulatedNotification.value = notification
        }
    }

    fun uploadExamWithQuestions(
        course: Course,
        examTitle: String,
        durationMin: Int,
        questions: List<Triple<String, List<String>, String>> // Question text, options, correct choice ("A", "B", "C", "D")
    ) {
        viewModelScope.launch {
            val examId = repository.insertExam(
                Exam(
                    courseId = course.id,
                    courseTitle = course.title,
                    title = examTitle,
                    durationMinutes = durationMin,
                    totalQuestions = questions.size
                )
            )

            val questionsList = questions.map { q ->
                Question(
                    examId = examId,
                    questionText = q.first,
                    optionA = q.second.getOrNull(0) ?: "",
                    optionB = q.second.getOrNull(1) ?: "",
                    optionC = q.second.getOrNull(2) ?: "",
                    optionD = q.second.getOrNull(3) ?: "",
                    correctOption = q.third
                )
            }
            repository.insertQuestions(questionsList)

            // Auto-trigger announcement
            val notification = PushNotification(
                title = "New Exam Added!",
                body = "The primary exam \"$examTitle\" is now available for Course: ${course.title}."
            )
            repository.insertNotification(notification)
            _lastSimulatedNotification.value = notification
        }
    }

    fun scheduleLiveClass(title: String, description: String, time: String) {
        viewModelScope.launch {
            repository.insertLiveClass(
                LiveClass(
                    title = title,
                    description = description,
                    scheduledTime = time,
                    isLiveNow = true
                )
            )

            val notification = PushNotification(
                title = "Live Class Scheduled!",
                body = "\"$title\" is starting right now! Tap to join the live session."
            )
            repository.insertNotification(notification)
            _lastSimulatedNotification.value = notification
        }
    }

    // Prepopulate DB
    private suspend fun prepopulateDatabase() {
        // Courses
        val c1Id = repository.insertCourse(
            Course(
                title = "Android UI with Jetpack Compose",
                description = "Master native UI development on Android using Jetpack Compose. Build responsive and beautiful layouts, manage states, and design delightful user flows offline.",
                category = "Mobile Development",
                duration = "6 Weeks",
                difficulty = "Beginner",
                content = "Jetpack Compose is Android's modern toolkit for building native UI. It simplifies and accelerates UI development on Android with less code, powerful tools, and intuitive Kotlin APIs. In this course, you will learn the mental shift of declarative UI, compose state management, modifiers, theme styles, animations, layouts, and dynamic canvas architectures.",
                bannerUrl = ""
            )
        )

        val c2Id = repository.insertCourse(
            Course(
                title = "Kotlin Programming Essentials",
                description = "Master the fundamentals of Kotlin, the official modern language for Android development. Explore syntax, variables, OOP, functional code, and asynchronous Coroutines.",
                category = "Computer Science",
                duration = "4 Weeks",
                difficulty = "Beginner",
                content = "Kotlin is a modern, statically typed programming language used by over 95% of the top Android apps. This comprehensive guide details basic syntax, null safety, lambdas, extensions, objects, high-order functions, and asynchronous concurrency flows via Coroutines. It's built to prepare you directly for production e-learning projects.",
                bannerUrl = ""
            )
        )

        val c3Id = repository.insertCourse(
            Course(
                title = "Deep Dive into Generative AI APIs",
                description = "Learn how to build AI-powered apps with the Gemini API. Understand prompt engineering, structured JSON output formats, functional calling, and multimodal inputs.",
                category = "Artificial Intelligence",
                duration = "5 Weeks",
                difficulty = "Intermediate",
                content = "Artificial intelligence is reshaping software. This course teaches developers how to leverage Google's Gemini models for text, audio, images, and video processing. We explore semantic structures, prompt strategies, temperature settings, structuring JSON responses using ResponseFormat schemas, and connecting Gemini to real-time functions.",
                bannerUrl = ""
            )
        )

        // Exams and Questions
        val exam1Id = repository.insertExam(
            Exam(
                courseId = c1Id,
                courseTitle = "Android UI with Jetpack Compose",
                title = "Jetpack Compose Core Concepts",
                durationMinutes = 10,
                totalQuestions = 3
            )
        )
        repository.insertQuestions(
            listOf(
                Question(
                    examId = exam1Id,
                    questionText = "Which function annotation is required to create a UI component in Jetpack Compose?",
                    optionA = "@ComposeUI",
                    optionB = "@Composable",
                    optionC = "@Component",
                    optionD = "@View",
                    correctOption = "B"
                ),
                Question(
                    examId = exam1Id,
                    questionText = "How do you preserve state across recomposition in Jetpack Compose?",
                    optionA = "Using remember { mutableStateOf(...) }",
                    optionB = "Using standard global variables",
                    optionC = "Using the standard saveState() method",
                    optionD = "Using properties inside custom Views",
                    correctOption = "A"
                ),
                Question(
                    examId = exam1Id,
                    questionText = "Which layout component is used to align elements vertically in Compose?",
                    optionA = "Row",
                    optionB = "Box",
                    optionC = "Column",
                    optionD = "Grid",
                    correctOption = "C"
                )
            )
        )

        val exam2Id = repository.insertExam(
            Exam(
                courseId = c2Id,
                courseTitle = "Kotlin Programming Essentials",
                title = "Kotlin Syntax & Null Safety Quiz",
                durationMinutes = 10,
                totalQuestions = 3
            )
        )
        repository.insertQuestions(
            listOf(
                Question(
                    examId = exam2Id,
                    questionText = "How does Kotlin handle potential NullPointerExceptions at compile time?",
                    optionA = "By automatically wrapping all variables in optionals",
                    optionB = "By making types non-nullable by default unless explicitly marked with a '?'",
                    optionC = "By throwing compiler errors on any assignments containing null",
                    optionD = "Through standard try/catch wrappers generated during compilation",
                    correctOption = "B"
                ),
                Question(
                    examId = exam2Id,
                    questionText = "Which keyword is used to declare a read-only variable in Kotlin?",
                    optionA = "const",
                    optionB = "let",
                    optionC = "var",
                    optionD = "val",
                    correctOption = "D"
                ),
                Question(
                    examId = exam2Id,
                    questionText = "What is the primary purpose of Kotlin Coroutines?",
                    optionA = "To write asynchronous, non-blocking code in a sequential style",
                    optionB = "To implement standard multi-inheritance class hierarchies",
                    optionC = "To manage local file-based database queries",
                    optionD = "To optimize local network interface parameters",
                    correctOption = "A"
                )
            )
        )

        // Live Classes
        repository.insertLiveClass(
            LiveClass(
                title = "Jetpack Compose Interactive State Workshop",
                description = "Join our instructor-led coding session as we build a real-time reactive canvas. Post questions, code along offline, and learn best state practices.",
                instructor = "Prof. Salish",
                scheduledTime = "Scheduled: Today, 8:00 PM",
                isLiveNow = true
            )
        )

        repository.insertLiveClass(
            LiveClass(
                title = "Gemini AI API Prompt Engineering Bootcamp",
                description = "Master semantic prompts, system instructions, and temperature tuning live on stream. Perfect for integrating AI chatbots in your apps.",
                instructor = "Dr. Anna",
                scheduledTime = "Scheduled: Tomorrow, 6:00 PM",
                isLiveNow = false
            )
        )

        // Notification
        repository.insertNotification(
            PushNotification(
                title = "Welcome to Salish eLearning!",
                body = "Enroll in courses, join mock live classes, take exams offline, and chat with your AI assistant Salish AI!"
            )
        )
    }

    private fun startSimulationEngine() {
        // Periodically trigger a simulated educational push notification to keep things dynamic and engage students
        viewModelScope.launch {
            delay(45000) // first simulation in 45s
            while (true) {
                val notificationsList = listOf(
                    PushNotification(
                        title = "Study Tip of the Day!",
                        body = "Take a 5-minute breather after completing a course section. It improves memory consolidation offline!"
                    ),
                    PushNotification(
                        title = "Salish Live Classes Notice",
                        body = "Did you know you can join live classrooms in mock stream mode and write messages offline? Join today!"
                    ),
                    PushNotification(
                        title = "Salish AI Tutor is online!",
                        body = "Stuck on any homework or programming question? Tap Chatbot tab and ask Salish AI anytime!"
                    )
                )
                val randomNotif = notificationsList.random()
                repository.insertNotification(randomNotif)
                _lastSimulatedNotification.value = randomNotif
                delay(90000) // repeat every 90 seconds
            }
        }
    }
}
