package com.wordlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshWord()

        findViewById<Button>(R.id.startOverlayBtn).setOnClickListener {
            val serviceIntent = Intent(this, WordNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "WordLock enabled! Check your lock screen.", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.newWordBtn).setOnClickListener {
            val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            val word = WordProvider.getRandomWord(this)
            val notification = Notification.Builder(this, WordNotificationService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(word.word)
                .setContentText(word.meaningNP)
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText("${word.word}  ${word.pronunciation}\n\n${word.meaning}\n\n${word.meaningNP}")
                        .setBigContentTitle(word.word)
                        .setSummaryText("${word.category.uppercase()} \u2022 ${word.pronunciation}")
                )
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .build()
            manager.notify(WordNotificationService.NOTIFICATION_ID, notification)
            refreshWord()
            Toast.makeText(this, "Notification updated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshWord() {
        val dw = WordProvider.getRandomWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP
    }

    override fun onResume() {
        super.onResume()
        refreshWord()
    }
}
