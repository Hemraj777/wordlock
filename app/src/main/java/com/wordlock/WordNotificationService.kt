package com.wordlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log

class WordNotificationService : Service() {

    private var screenReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val word = WordProvider.getRandomWord(this)
        startForeground(NOTIFICATION_ID, buildNotification(word))
        registerScreenReceiver()
        Log.d("WordLock", "Service started")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerScreenReceiver() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    val word = WordProvider.getRandomWord(context)
                    val manager = getSystemService(NotificationManager::class.java)
                    manager.notify(NOTIFICATION_ID, buildNotification(word))
                    Log.d("WordLock", "Updated word: ${word.word}")
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
        val fullText = "${word.word}  ${word.pronunciation}\n\n${word.meaning}\n\n${word.meaningNP}"

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(word.word)
            .setContentText(word.meaningNP)
            .setStyle(
                Notification.BigTextStyle()
                    .bigText(fullText)
                    .setBigContentTitle(word.word)
                    .setSummaryText("${word.category.uppercase()} \u2022 ${word.pronunciation}")
            )
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WordLock",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily word on lock screen"
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
    }

    companion object {
        const val CHANNEL_ID = "wordlock_words"
        const val NOTIFICATION_ID = 7777
    }
}
