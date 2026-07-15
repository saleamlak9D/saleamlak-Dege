package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.Course
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Upload Course, 1: Add Primary Exam, 2: Schedule Live Class

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Salish Manager Studio",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "As a Manager, publish courses, assessments, and broadcast mock live streams locally.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom tab indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val tabs = listOf("New Course", "Add Exam", "Live Session")
            tabs.forEachIndexed { index, label ->
                val isSelected = activeTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { activeTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> UploadCourseForm(onUpload = { title, desc, cat, dur, diff, content ->
                    viewModel.uploadCourse(title, desc, cat, dur, diff, content)
                })
                1 -> CreateExamForm(
                    courses = courses,
                    onSaveExam = { course, title, minutes, questions ->
                        viewModel.uploadExamWithQuestions(course, title, minutes, questions)
                    }
                )
                2 -> ScheduleLiveForm(onSchedule = { title, desc, time ->
                    viewModel.scheduleLiveClass(title, desc, time)
                })
            }
        }
    }
}

@Composable
fun UploadCourseForm(
    onUpload: (String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Publish Course details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Course Title (e.g. Kotlin OOP Essentials)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("course_title_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Brief Description (Summary of course)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                maxLines = 2
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. AI)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g. 3 Weeks)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = difficulty,
                onValueChange = { difficulty = it },
                label = { Text("Difficulty level (Beginner/Intermediate/Advanced)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Course Curriculum / Full Text Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(8.dp),
                maxLines = 10
            )
        }

        item {
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = Color(0xFF0D9488),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || category.isBlank() || content.isBlank()) {
                        statusMessage = "Please complete all mandatory fields!"
                    } else {
                        onUpload(title, description, category, duration.ifBlank { "4 Weeks" }, difficulty.ifBlank { "Beginner" }, content)
                        statusMessage = "Course successfully published offline! Students notified."
                        title = ""
                        description = ""
                        category = ""
                        duration = ""
                        difficulty = ""
                        content = ""
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("publish_course_button")
            ) {
                Text("Publish Course Offline", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamForm(
    courses: List<Course>,
    onSaveExam: (Course, String, Int, List<Triple<String, List<String>, String>>) -> Unit
) {
    if (courses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Please publish at least one course before adding primary exams!",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    var examTitle by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("15") }
    var selectedCourse by remember { mutableStateOf(courses.first()) }
    var expandedCourseDropdown by remember { mutableStateOf(false) }

    // Hardcoded question fields for simplified high-polish creation (Up to 2 questions)
    var q1Text by remember { mutableStateOf("") }
    var q1A by remember { mutableStateOf("") }
    var q1B by remember { mutableStateOf("") }
    var q1C by remember { mutableStateOf("") }
    var q1D by remember { mutableStateOf("") }
    var q1Correct by remember { mutableStateOf("A") }

    var q2Text by remember { mutableStateOf("") }
    var q2A by remember { mutableStateOf("") }
    var q2B by remember { mutableStateOf("") }
    var q2C by remember { mutableStateOf("") }
    var q2D by remember { mutableStateOf("") }
    var q2Correct by remember { mutableStateOf("B") }

    var statusMessage by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Publish Primary Exam Assessment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        item {
            // Course drop down
            ExposedDropdownMenuBox(
                expanded = expandedCourseDropdown,
                onExpandedChange = { expandedCourseDropdown = !expandedCourseDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedCourse.title,
                    onValueChange = {},
                    label = { Text("Select Target Course") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourseDropdown) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCourseDropdown,
                    onDismissRequest = { expandedCourseDropdown = false }
                ) {
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course.title) },
                            onClick = {
                                selectedCourse = course
                                expandedCourseDropdown = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Exam Title") },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = { Text("Duration (mins)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Assessment Question 1", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            OutlinedTextField(
                value = q1Text,
                onValueChange = { q1Text = it },
                label = { Text("Question 1 text") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = q1A, onValueChange = { q1A = it }, label = { Text("Option A") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = q1B, onValueChange = { q1B = it }, label = { Text("Option B") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = q1C, onValueChange = { q1C = it }, label = { Text("Option C") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = q1D, onValueChange = { q1D = it }, label = { Text("Option D") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
            }
        }

        item {
            // Correct Choice Dropdown simple segment
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Correct Option: ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                listOf("A", "B", "C", "D").forEach { choice ->
                    val isSel = q1Correct == choice
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { q1Correct = choice }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(choice, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Assessment Question 2 (Optional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            OutlinedTextField(
                value = q2Text,
                onValueChange = { q2Text = it },
                label = { Text("Question 2 text") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = q2A, onValueChange = { q2A = it }, label = { Text("Option A") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = q2B, onValueChange = { q2B = it }, label = { Text("Option B") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = q2C, onValueChange = { q2C = it }, label = { Text("Option C") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = q2D, onValueChange = { q2D = it }, label = { Text("Option D") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Correct Option: ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                listOf("A", "B", "C", "D").forEach { choice ->
                    val isSel = q2Correct == choice
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { q2Correct = choice }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(choice, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = Color(0xFF0D9488),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (examTitle.isBlank() || q1Text.isBlank() || q1A.isBlank() || q1B.isBlank()) {
                        statusMessage = "Please add at least an Exam Title and Question 1 details!"
                    } else {
                        val questionsList = mutableListOf<Triple<String, List<String>, String>>()
                        questionsList.add(Triple(q1Text, listOf(q1A, q1B, q1C, q1D), q1Correct))

                        if (q2Text.isNotBlank() && q2A.isNotBlank() && q2B.isNotBlank()) {
                            questionsList.add(Triple(q2Text, listOf(q2A, q2B, q2C, q2D), q2Correct))
                        }

                        onSaveExam(selectedCourse, examTitle, durationMinutes.toIntOrNull() ?: 15, questionsList)
                        statusMessage = "Exam successfully saved and published!"
                        examTitle = ""
                        q1Text = ""
                        q1A = ""
                        q1B = ""
                        q1C = ""
                        q1D = ""
                        q2Text = ""
                        q2A = ""
                        q2B = ""
                        q2C = ""
                        q2D = ""
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("publish_exam_button")
            ) {
                Text("Publish Primary Exam Assessment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScheduleLiveForm(
    onSchedule: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var timeString by remember { mutableStateOf("Scheduled: Today, 8:00 PM") }

    var statusMessage by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Schedule Broadcast Live Class", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Live Session Topic") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Brief curriculum focus description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            maxLines = 3
        )

        OutlinedTextField(
            value = timeString,
            onValueChange = { timeString = it },
            label = { Text("Broadcast Time (e.g., Today, 9:00 PM)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = Color(0xFF0D9488),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Button(
            onClick = {
                if (title.isBlank() || description.isBlank()) {
                    statusMessage = "All fields are mandatory!"
                } else {
                    onSchedule(title, description, timeString)
                    statusMessage = "Live classroom broadcast scheduled!"
                    title = ""
                    description = ""
                }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Schedule & Start Session", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.LiveTv, contentDescription = null)
        }
    }
}
