package com.pharma.taskmanager.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pharma.taskmanager.data.database.TaskEntity
import com.pharma.taskmanager.services.ReminderBroadcastReceiver
import com.pharma.taskmanager.services.UltimateReminderService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UltimateReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun scheduleReminder(task: TaskEntity) {
        task.reminderTime?.let { reminderTime ->
            if (reminderTime > System.currentTimeMillis()) {
                val alarmIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                    putExtra(UltimateReminderService.EXTRA_TASK_ID, task.id)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.id,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    // Fallback to regular alarm if exact alarm permission not granted
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                }
            }
        }
    }
    
    fun cancelReminder(taskId: Int) {
        val alarmIntent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
    
    fun schedulePriorityBasedReminders(tasks: List<TaskEntity>) {
        // Sort by priority (3=High, 2=Medium, 1=Low) and due time
        val sortedTasks = tasks
            .filter { it.status == "pending" && it.reminderTime != null }
            .sortedWith(compareByDescending<TaskEntity> { it.priority }.thenBy { it.reminderTime })
        
        // Schedule each task with slight delay for priority ordering
        sortedTasks.forEachIndexed { index, task ->
            val adjustedReminderTime = (task.reminderTime ?: 0) + (index * 1000) // 1 second delay between priorities
            val adjustedTask = task.copy(reminderTime = adjustedReminderTime)
            scheduleReminder(adjustedTask)
        }
    }
    
    fun triggerImmediateReminder(task: TaskEntity) {
        UltimateReminderService.startReminderService(context, task)
    }
}