package com.pharma.taskmanager.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.hilt.navigation.compose.hiltViewModel
import com.pharma.taskmanager.ui.viewmodel.TaskViewModel
import com.pharma.taskmanager.utils.SampleDataProvider
import com.pharma.taskmanager.ui.components.ReminderDialog
import com.pharma.taskmanager.ui.components.AutoReminderSystem
import com.pharma.taskmanager.data.database.TaskEntity
import com.pharma.taskmanager.data.database.TaskConstants
import com.pharma.taskmanager.ui.theme.*
import com.pharma.taskmanager.services.UltimateReminderService
import com.pharma.taskmanager.utils.UltimateReminderManager
import android.content.Intent
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    // Context will be added later
    val taskStats by viewModel.taskStats.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showReminderDialog by remember { mutableStateOf(false) }
    var currentReminderTask by remember { mutableStateOf<TaskEntity?>(null) }
    
    // Sample task for demo
    val sampleTask = TaskEntity(
        id = 0,
        title = "Complete Project Presentation",
        description = "Prepare and finalize the quarterly project presentation for the team meeting",
        dueDateTime = System.currentTimeMillis(),
        priority = TaskConstants.PRIORITY_HIGH,
        status = "pending",
        reminderTime = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis()
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "💎 BEAUTIFUL TASKS ELITE 💎",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                shadow = Shadow(
                    color = EliteGold,
                    offset = Offset(8f, 8f),
                    blurRadius = 24f
                )
            ),
            textAlign = TextAlign.Center,
            color = EliteCrystal,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        Text(
            text = "✨ PREMIUM • ELITE • PERFECTION ✨",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = EliteViolet,
                    offset = Offset(4f, 4f),
                    blurRadius = 12f
                )
            ),
            textAlign = TextAlign.Center,
            color = EliteGold,
            modifier = Modifier.padding(bottom = 40.dp)
        )
        
        Text(
            text = "💡 Welcome to your beautiful task management experience! Create tasks, set smart reminders with priority alerts, and stay organized with style!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Task Statistics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quick Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${taskStats.total}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${taskStats.pending}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${taskStats.completed}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    if (taskStats.overdue > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${taskStats.overdue}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Overdue",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Button(
            onClick = onNavigateToTasks,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("View All Tasks")
        }
        
        // 💎 ELITE REMINDER TEST BUTTON 💎
        Button(
            onClick = { 
                currentReminderTask = sampleTask
                showReminderDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EliteViolet
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationImportant,
                contentDescription = "Elite Test",
                modifier = Modifier.size(32.dp),
                tint = EliteGold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "💎 ELITE REMINDER TEST 💎",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = EliteCrystal,
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = Shadow(
                        color = EliteGold,
                        offset = Offset(2f, 2f),
                        blurRadius = 8f
                    )
                )
            )
        }
        
        // Add sample data button (for testing)
        if (taskStats.total == 0) {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        SampleDataProvider.getSampleTasks().forEach { sampleTask ->
                            viewModel.createTask(
                                title = sampleTask.title,
                                description = sampleTask.description,
                                dueDateTime = sampleTask.dueDateTime,
                                priority = sampleTask.priority,
                                reminderTime = sampleTask.reminderTime
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Add Sample Tasks (For Testing)")
            }
        }
    }
    
    // AUTO REMINDER SYSTEM
    AutoReminderSystem(
        tasks = allTasks,
        onShowReminder = { task ->
            currentReminderTask = task
            showReminderDialog = true
        }
    )
    
    // REMINDER DIALOG
    if (showReminderDialog && currentReminderTask != null) {
        ReminderDialog(
            task = currentReminderTask!!,
            onDismiss = { showReminderDialog = false },
            onComplete = { 
                showReminderDialog = false
                // Task completion functionality
            },
            onSnooze = { 
                showReminderDialog = false
                // Handle snooze
            }
        )
    }
}