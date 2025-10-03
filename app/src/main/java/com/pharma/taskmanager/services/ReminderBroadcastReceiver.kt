package com.pharma.taskmanager.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pharma.taskmanager.data.database.TaskConstants

class ReminderBroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(UltimateReminderService.EXTRA_TASK_ID, -1)
        val taskTitle = intent.getStringExtra(UltimateReminderService.EXTRA_TASK_TITLE) ?: "Task Reminder"
        val taskPriority = intent.getIntExtra(UltimateReminderService.EXTRA_TASK_PRIORITY, 2)
        
        if (taskId != -1) {
            // Create a sample task for the reminder
            val sampleTask = com.pharma.taskmanager.data.database.TaskEntity(
                id = taskId,
                title = taskTitle,
                description = "Reminder for your task",
                dueDateTime = System.currentTimeMillis(),
                priority = taskPriority,
                status = "pending",
                reminderTime = System.currentTimeMillis()
            )
            UltimateReminderService.startReminderService(context, sampleTask)
        }
    }
}