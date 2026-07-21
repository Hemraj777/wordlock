package com.wordlock

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_SCREEN_ON) return

        val prefs = context.getSharedPreferences("wordlock", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return

        val word = WordProvider.getRandomWord(context)

        val openIntent = Intent(context, LockWordActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingOpen = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "wl_words")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(word.word)
            .setContentText("${word.pronunciation}  ·  ${word.category}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${word.meaning}\n\n${word.meaningNP}\n\n${word.example}")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingOpen, true)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(7777, notification)
    }
}
