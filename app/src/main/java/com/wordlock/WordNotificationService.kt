package com.wordlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class WordNotificationService : Service() {

    private var screenReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("WordLock", "Service onCreate")
        createNotificationChannel()
        val word = WordProvider.getRandomWord(this)
        val notification = buildNotification(word)
        startForeground(NOTIFICATION_ID, notification)
        registerScreenReceiver()
        Log.d("WordLock", "Service started, foreground notification posted")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerScreenReceiver() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    val word = WordProvider.getRandomWord(context)
                    val manager = context.getSystemService(NotificationManager::class.java)
                    val notification = buildNotification(word)
                    manager.notify(NOTIFICATION_ID, notification)
                    Log.d("WordLock", "Updated notification: ${word.word}")
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
    }

    private fun buildNotification(word: Word): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullText = buildString {
            appendLine(word.word)
            appendLine(word.pronunciation)
            appendLine()
            appendLine(word.meaning)
            appendLine()
            appendLine(word.meaningNP)
        }.trimEnd()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(word.word)
            .setContentText(word.meaningNP)
            .setSubText(word.category.uppercase())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(fullText)
                    .setBigContentTitle("${word.word}  ${word.pronunciation}")
                    .setSummaryText("${word.category.uppercase()} \u2022 New word today")
            )
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setUsesChronometer(false)
            .setWhen(System.currentTimeMillis())
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WordLock",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows a new word on your lock screen"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)
            enableLights(false)
            setShowBadge(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        screenReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        Log.d("WordLock", "Service destroyed")
    }

    companion object {
        const val CHANNEL_ID = "wordlock_words"
        const val NOTIFICATION_ID = 7777
    }
}
