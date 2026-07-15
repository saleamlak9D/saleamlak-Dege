package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveClass
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

data class LocalLiveChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean = false,
    val timestamp: String = "Just Now"
)

@Composable
fun LiveClassScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val liveClasses by viewModel.liveClasses.collectAsState()
    val selectedLiveClass by viewModel.selectedLiveClass.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedLiveClass != null) {
            LiveClassRoomView(
                liveClass = selectedLiveClass!!,
                onLeave = { viewModel.selectLiveClass(null) }
            )
        } else {
            LiveClassListView(
                liveClasses = liveClasses,
                onJoin = { viewModel.selectLiveClass(it) }
            )
        }
    }
}

@Composable
fun LiveClassListView(
    liveClasses: List<LiveClass>,
    onJoin: (LiveClass) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Live Interactive Classrooms",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Join simulated streaming lectures, interact with peers, and study offline.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val activeClasses = liveClasses.filter { it.isLiveNow }
        val scheduledClasses = liveClasses.filter { !it.isLiveNow }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (activeClasses.isEmpty() && scheduledClasses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No live classes scheduled.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Switch role to Manager in profile and schedule/start a live class session!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (activeClasses.isNotEmpty()) {
                item {
                    Text(
                        text = "LIVE NOW",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(activeClasses) { item ->
                    ActiveLiveClassCard(liveClass = item, onJoin = { onJoin(item) })
                }
            }

            if (scheduledClasses.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "UPCOMING SCHEDULE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(scheduledClasses) { item ->
                    ScheduledClassCard(liveClass = item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveLiveClassCard(
    liveClass: LiveClass,
    onJoin: () -> Unit
) {
    // Pulse animation for the LIVE badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val badgeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        onClick = onJoin,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_class_card_${liveClass.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = badgeAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE CLASSROOM",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    )
                }

                Badge(
                    containerColor = Color(0xFF0D9488),
                    contentColor = Color.White
                ) {
                    Text(
                        text = "Interactive",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = liveClass.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = liveClass.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = liveClass.instructor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("join_live_button")
                ) {
                    Text("Join Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScheduledClassCard(
    liveClass: LiveClass
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = liveClass.scheduledTime,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = liveClass.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Instructor: ${liveClass.instructor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Scheduled",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LiveClassRoomView(
    liveClass: LiveClass,
    onLeave: () -> Unit
) {
    var userCommentText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Initialize mock chat messages
    val chatMessages = remember {
        mutableStateListOf(
            LocalLiveChatMessage("Liam (Study Host)", "Welcome to the Salish live classroom! Type questions below.", false),
            LocalLiveChatMessage("Emma", "This is so cool! Does this play offline too?", false),
            LocalLiveChatMessage("Noah", "Yes! Salish eLearning saves lecture progress fully.", false)
        )
    }

    // Launch periodic mock chat updates to simulate real-time student interaction
    LaunchedEffect(key1 = true) {
        val mockStudents = listOf("Sophia", "Mason", "Isabella", "Lucas", "Olivia", "James", "Ava", "Henry")
        val mockTexts = listOf(
            "Wow, this whiteboard diagram makes Kotlin so intuitive!",
            "Can we review primary exams after submitting?",
            "Salish AI chatbot answers these questions so quickly in the next tab!",
            "Agreed! I just asked the AI about Compose State.",
            "Professor Salish represents the coastal study group so well.",
            "I am ready for the Jetpack assessment!",
            "Is recomposition triggered automatically on state change?",
            "Yes Sophia! That is Compose's core capability."
        )

        while (true) {
            delay(6000)
            val randomStudent = mockStudents.random()
            val randomText = mockTexts.random()
            chatMessages.add(LocalLiveChatMessage(randomStudent, randomText))
            // Scroll to the bottom of list
            scope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }

    // Interactive animated streaming canvas
    val infiniteTransition = rememberInfiniteTransition(label = "canvasAnim")
    val pulseFreq by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frequency"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Stream Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLeave) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Leave Class")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = liveClass.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Text(
                        text = "Broadcasting with Prof. Salish",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEF4444))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Mock Dynamic Streaming Video Canvas (ocean-themed wave diagram represent active session!)
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val canvasBg = MaterialTheme.colorScheme.inverseOnSurface

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(canvasBg)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val midY = height / 2

                // Draw coordinate lines
                drawLine(
                    color = primaryColor.copy(alpha = 0.15f),
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 2.dp.toPx()
                )

                // Draw ocean wave study animation
                val pathPoints = mutableListOf<Offset>()
                for (x in 0..width.toInt() step 5) {
                    val angle = (x.toFloat() / width) * 4 * Math.PI.toFloat() + pulseFreq
                    val y = midY + sin(angle) * 40.dp.toPx()
                    pathPoints.add(Offset(x.toFloat(), y))
                }

                for (i in 0 until pathPoints.size - 1) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.8f),
                        start = pathPoints[i],
                        end = pathPoints[i + 1],
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Draw a secondary sine wave to show interactive spectrum
                val secondPathPoints = mutableListOf<Offset>()
                for (x in 0..width.toInt() step 5) {
                    val angle = (x.toFloat() / width) * 5 * Math.PI.toFloat() - pulseFreq
                    val y = midY + sin(angle) * 20.dp.toPx()
                    secondPathPoints.add(Offset(x.toFloat(), y))
                }

                for (i in 0 until secondPathPoints.size - 1) {
                    drawLine(
                        color = secondaryColor.copy(alpha = 0.5f),
                        start = secondPathPoints[i],
                        end = secondPathPoints[i + 1],
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Controls overlaid on video
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.SignalCellularAlt, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1080p HD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Offline Local Feed", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Direct playback watermark
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Playing", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Interactive Classroom Visualizer", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Action Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    chatMessages.add(LocalLiveChatMessage("Liam (Study Host)", "Hand raised! Prof. Salish has been notified of your question.", false))
                    scope.launch { listState.animateScrollToItem(chatMessages.size - 1) }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(imageVector = Icons.Default.BackHand, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Raise Hand", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    chatMessages.add(LocalLiveChatMessage("User", "Asked Question: \"How do we manage state safely offline?\"", true))
                    chatMessages.add(LocalLiveChatMessage("Liam (Study Host)", "Good question! Addressed to Prof. Salish.", false))
                    scope.launch { listState.animateScrollToItem(chatMessages.size - 1) }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ask Question", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Class Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Live Chat List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.sender,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                        bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                    )
                                )
                                .background(
                                    if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Chat Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userCommentText,
                    onValueChange = { userCommentText = it },
                    placeholder = { Text("Comment in classroom...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("classroom_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    maxLines = 1,
                    textStyle = TextStyle(fontSize = 14.sp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (userCommentText.isNotBlank()) {
                            chatMessages.add(LocalLiveChatMessage("Student (You)", userCommentText, true))
                            userCommentText = ""
                            scope.launch {
                                delay(100)
                                listState.animateScrollToItem(chatMessages.size - 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("send_classroom_comment_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
