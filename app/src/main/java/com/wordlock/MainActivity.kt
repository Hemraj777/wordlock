package com.wordlock

import android.app.Notification
import android.app.NotificationManager
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
            refreshWord()
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
