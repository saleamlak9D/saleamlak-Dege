package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.SalishELearningTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.UserRole
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalishELearningTheme {
                MainAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val viewModel: MainViewModel = viewModel()
    val currentRole by viewModel.currentRole.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val lastNotification by viewModel.lastSimulatedNotification.collectAsState()
    val finishedResult by viewModel.examFinishedResult.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    var activeScreenIndex by remember { mutableStateOf(0) } // 0: Dashboard, 1: Live, 2: AI, 3: Manager, 4: Profile

    // Floating Push Notification banner controller
    var showPushBanner by remember { mutableStateOf(false) }
    var bannerTitle by remember { mutableStateOf("") }
    var bannerBody by remember { mutableStateOf("") }

    LaunchedEffect(lastNotification) {
        if (lastNotification != null) {
            bannerTitle = lastNotification!!.title
            bannerBody = lastNotification!!.body
            showPushBanner = true
            delay(5000) // auto dismiss banner after 5 seconds
            showPushBanner = false
            viewModel.clearLastSimulatedNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Offline Mode Active indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.testTag("offline_mode_indicator")
                    ) {
                        val pulseTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by pulseTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseDot"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isOnline) Color(0xFF2E7D32).copy(alpha = pulseAlpha)
                                    else Color(0xFFD84315).copy(alpha = pulseAlpha)
                                )
                        )
                        Text(
                            text = if (isOnline) "Online Mode Active" else "Offline Mode Active",
                            color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFD84315),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Notification & Profile action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Notifications
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { activeScreenIndex = 4 }
                                .testTag("notification_icon_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Simulated Push Notifications",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            if (unreadNotificationsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 1.dp, y = (-1).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFBA1A1A))
                                )
                            }
                        }

                        // Avatar (role switch indicator)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { viewModel.toggleRole() }
                                .testTag("quick_role_switch"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentRole == UserRole.STUDENT) "S" else "M",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Title
                Text(
                    text = "Salish eLearning",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (currentRole == UserRole.STUDENT) "Welcome back, Student" else "Welcome back, Manager",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeScreenIndex == 0,
                    onClick = { activeScreenIndex = 0 },
                    icon = { Icon(imageVector = Icons.Default.School, contentDescription = "Courses") },
                    label = { Text("Courses", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_courses")
                )

                NavigationBarItem(
                    selected = activeScreenIndex == 1,
                    onClick = { activeScreenIndex = 1 },
                    icon = { Icon(imageVector = Icons.Default.LiveTv, contentDescription = "Live Class") },
                    label = { Text("Live Class", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_live_class")
                )

                NavigationBarItem(
                    selected = activeScreenIndex == 2,
                    onClick = { activeScreenIndex = 2 },
                    icon = { Icon(imageVector = Icons.Default.Chat, contentDescription = "Salish AI chatbot") },
                    label = { Text("Salish AI", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_chatbot")
                )

                // Only visible to users who are Manager
                if (currentRole == UserRole.MANAGER) {
                    NavigationBarItem(
                        selected = activeScreenIndex == 3,
                        onClick = { activeScreenIndex = 3 },
                        icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Manager Studio") },
                        label = { Text("Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_manager_studio")
                    )
                }

                NavigationBarItem(
                    selected = activeScreenIndex == 4,
                    onClick = { activeScreenIndex = 4 },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile and Results") },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            when (activeScreenIndex) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> LiveClassScreen(viewModel = viewModel)
                2 -> ChatbotScreen(viewModel = viewModel)
                3 -> {
                    if (currentRole == UserRole.MANAGER) {
                        ManagerStudioScreen(viewModel = viewModel)
                    } else {
                        activeScreenIndex = 0 // Fallback if role is toggled away
                    }
                }
                4 -> ProfileResultsScreen(viewModel = viewModel)
            }

            // Real-Time Push Notification overlay alert banner
            AnimatedVisibility(
                visible = showPushBanner,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showPushBanner = false
                            activeScreenIndex = 4 // jump to notifications inbox tab on click
                        }
                        .testTag("simulated_push_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bannerTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bannerBody,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { showPushBanner = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss push alert",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Celebratory Assessment submission reward overlay
            AnimatedVisibility(
                visible = finishedResult != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                if (finishedResult != null) {
                    val result = finishedResult!!
                    val isPassed = result.percentage >= 60.0

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("result_celebration_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPassed) Color(0xFF0D9488).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPassed) Icons.Default.Celebration else Icons.Default.Feedback,
                                        contentDescription = null,
                                        tint = if (isPassed) Color(0xFF0D9488) else Color(0xFFEF4444),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if (isPassed) "Congratulations!" else "Keep practicing!",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = if (isPassed) Color(0xFF0D9488) else Color(0xFFEF4444)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "You have completed your primary exam assessment.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = result.examTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Course: ${result.courseTitle}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = String.format("%.0f%%", result.percentage),
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isPassed) Color(0xFF0D9488) else Color(0xFFEF4444)
                                        )

                                        Text(
                                            text = "Correct: ${result.score} of ${result.totalQuestions} questions",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        viewModel.clearFinishedResult()
                                        activeScreenIndex = 4 // redirect to Profile Results tab
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("View Score Archive", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = { viewModel.clearFinishedResult() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Dismiss", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
