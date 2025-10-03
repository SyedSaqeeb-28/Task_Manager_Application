package com.pharma.taskmanager.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.pharma.taskmanager.R
import com.pharma.taskmanager.data.database.TaskEntity
import com.pharma.taskmanager.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UltimateReminderService : Service() {
    
    companion object {
        const val CHANNEL_ID = "ultimate_reminder_channel"
        const val NOTIFICATION_ID = 12345
        const val ACTION_STOP_REMINDER = "STOP_REMINDER"
        const val ACTION_SNOOZE = "SNOOZE_REMINDER"
        const val ACTION_COMPLETE = "COMPLETE_TASK"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_TASK_PRIORITY = "task_priority"
        
        fun startReminderService(context: Context, task: TaskEntity) {
            val intent = Intent(context, UltimateReminderService::class.java).apply {
                putExtra(EXTRA_TASK_ID, task.id)
                putExtra(EXTRA_TASK_TITLE, task.title)
                putExtra(EXTRA_TASK_PRIORITY, task.priority)
            }
            context.startForegroundService(intent)
        }
        
        fun stopReminderService(context: Context) {
            context.stopService(Intent(context, UltimateReminderService::class.java))
        }
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentTaskId: Int = -1
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val taskId = it.getIntExtra(EXTRA_TASK_ID, -1)
            val taskTitle = it.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
            val taskPriority = it.getIntExtra(EXTRA_TASK_PRIORITY, 2)
            
            when (it.action) {
                ACTION_STOP_REMINDER -> {
                    stopReminder()
                    stopSelf()
                }
                ACTION_SNOOZE -> {
                    stopReminder()
                    scheduleSnooze(taskId)
                    stopSelf()
                }
                ACTION_COMPLETE -> {
                    stopReminder()
                    // TODO: Mark task as complete in database
                    stopSelf()
                }
                else -> {
                    currentTaskId = taskId
                    startReminder(taskId, taskTitle, taskPriority)
                }
            }
        }
        
        return START_STICKY
    }
    
    private fun startReminder(taskId: Int, taskTitle: String, priority: Int) {
        // Create foreground notification
        val notification = createReminderNotification(taskId, taskTitle, priority)
        startForeground(NOTIFICATION_ID, notification)
        
        // Start sound
        playReminderSound()
        
        // Start vibration
        startVibration(priority)
        
        // Auto-stop after 2 minutes if not dismissed
        Handler(Looper.getMainLooper()).postDelayed({
            stopReminder()
            stopSelf()
        }, 2 * 60 * 1000) // 2 minutes
    }
    
    private fun stopReminder() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        
        vibrator?.cancel()
    }
    
    private fun playReminderSound() {
        try {
            mediaPlayer = MediaPlayer().apply {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                
                setDataSource(this@UltimateReminderService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startVibration(priority: Int) {
        val pattern = when (priority) {
            3 -> longArrayOf(0, 1000, 500, 1000, 500, 1000) // High priority - intense
            2 -> longArrayOf(0, 800, 400, 800) // Medium priority - moderate
            else -> longArrayOf(0, 500, 300) // Low priority - gentle
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }
    
    private fun createReminderNotification(taskId: Int, taskTitle: String, priority: Int): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Stop reminder action
        val stopIntent = Intent(this, UltimateReminderService::class.java).apply {
            action = ACTION_STOP_REMINDER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Snooze action
        val snoozeIntent = Intent(this, UltimateReminderService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this, 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Complete action
        val completeIntent = Intent(this, UltimateReminderService::class.java).apply {
            action = ACTION_COMPLETE
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getService(
            this, 3, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val priorityText = when (priority) {
            3 -> "🔥 URGENT TASK"
            2 -> "⚡ MEDIUM PRIORITY"
            else -> "✅ TASK REMINDER"
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$priorityText - Time to Act!")
            .setContentText(taskTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$taskTitle\n\nYour $priorityText task is due now! Take action immediately."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.ic_stop, "STOP", stopPendingIntent)
            .addAction(R.drawable.ic_snooze, "SNOOZE 10MIN", snoozePendingIntent)
            .addAction(R.drawable.ic_check, "DONE", completePendingIntent)
            .setFullScreenIntent(mainPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    private fun scheduleSnooze(taskId: Int) {
        val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
        
        val alarmIntent = Intent(this, ReminderBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this, taskId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
            )
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ultimate Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority task reminder notifications"
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}