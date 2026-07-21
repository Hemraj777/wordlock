package com.wordlock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dw = WordProvider.getDailyWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP

        findViewById<Button>(R.id.startOverlayBtn).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE
                    )
                    return@setOnClickListener
                }
            }

            val intent = Intent(this, WordOverlayService::class.java)
            startForegroundService(intent)
            Toast.makeText(this, "WordLock active! Check your lock screen.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.newWordBtn).setOnClickListener {
            val word = WordProvider.getRandomWord(this)
            findViewById<TextView>(R.id.wordText).text = word.word
            findViewById<TextView>(R.id.meaningText).text = word.meaning
            findViewById<TextView>(R.id.meaningNPText).text = word.meaningNP
            Toast.makeText(this, "New word!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, WordOverlayService::class.java)
                startForegroundService(intent)
                Toast.makeText(this, "WordLock active! Check your lock screen.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission required for lock screen", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val dw = WordProvider.getDailyWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP
    }
}
