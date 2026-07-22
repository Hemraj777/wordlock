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
    private var unlockReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("WordLock", "Service onCreate")
        createNotificationChannel()
        val word = WordProvider.getRandomWord(this)
        startForeground(NOTIFICATION_ID, buildNotification(word))
        registerReceivers()
        Log.d("WordLock", "Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerReceivers() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    val word = WordProvider.getRandomWord(context)
                    val manager = context.getSystemService(NotificationManager::class.java)
                    manager.notify(NOTIFICATION_ID, buildOverlayNotification(context, word))
                    Log.d("WordLock", "Overlay notification posted: ${word.word}")
                }
            }
        }

        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_USER_PRESENT) {
                    val manager = context.getSystemService(NotificationManager::class.java)
                    manager.cancel(NOTIFICATION_ID)
                    Log.d("WordLock", "Cancelled on unlock")
                }
            }
        }

        val screenFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        val unlockFilter = IntentFilter(Intent.ACTION_USER_PRESENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, screenFilter, Context.RECEIVER_NOT_EXPORTED)
            registerReceiver(unlockReceiver, unlockFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, screenFilter)
            registerReceiver(unlockReceiver, unlockFilter)
        }
    }

    private fun buildOverlayNotification(context: Context, word: Word): Notification {
        val overlayIntent = Intent(context, WordOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), overlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tapIntent = PendingIntent.getActivity(
            context, 0, overlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shortText = "${word.meaning}\n${word.meaningNP}"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(word.word)
            .setContentText(shortText)
            .setSubText("${word.category.uppercase()} \u2022 ${word.pronunciation}")
            .setContentIntent(tapIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSortKey("zzz")
            .setWhen(System.currentTimeMillis())
            .build()
    }

    private fun buildNotification(word: Word): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shortText = "${word.meaning}\n${word.meaningNP}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WordLock Active")
            .setContentText("New word on each screen unlock")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
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
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        screenReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        unlockReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        Log.d("WordLock", "Service destroyed")
    }

    companion object {
        const val CHANNEL_ID = "wordlock_v5"
        const val NOTIFICATION_ID = 7777
    }
}
