package com.pharma.taskmanager.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.pharma.taskmanager.data.database.TaskEntity
import com.pharma.taskmanager.data.database.TaskConstants
import kotlinx.coroutines.delay

@Composable
fun AutoReminderSystem(
    tasks: List<TaskEntity>,
    onShowReminder: (TaskEntity) -> Unit
) {
    val context = LocalContext.current
    
    LaunchedEffect(tasks) {
        while (true) {
            delay(60000) // Check every minute for accurate timing
            
            val currentTime = System.currentTimeMillis()
            
            // Find tasks with exact reminder time (within 1 minute window)
            val reminderTasks = tasks.filter { task ->
                task.status == "pending" &&
                task.reminderTime != null &&
                task.reminderTime <= currentTime &&
                (currentTime - task.reminderTime) < 60000 // Only within 1 minute of set reminder time
            }.sortedByDescending { 
                // Prioritize by priority level
                when (it.priority) {
                    TaskConstants.PRIORITY_HIGH -> 3
                    TaskConstants.PRIORITY_MEDIUM -> 2
                    else -> 1
                }
            }
            
            if (reminderTasks.isNotEmpty()) {
                onShowReminder(reminderTasks.first())
                delay(300000) // Wait 5 minutes before next check to avoid spam
            }
        }
    }
}