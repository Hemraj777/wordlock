package com.wordlock

import android.Manifest
import android.app.WallpaperManager
import android.content.ComponentName
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

    private val lockPref by lazy { getSharedPreferences("wordlock", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dw = WordProvider.getDailyWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP

        val enableBtn = findViewById<Button>(R.id.startOverlayBtn)
        val isEnabled = lockPref.getBoolean("enabled", false)
        enableBtn.text = if (isEnabled) "Disable" else "Enable Lock Screen Words"
        enableBtn.setOnClickListener {
            if (!isEnabled) {
                requestPermissions()
            } else {
                lockPref.edit().putBoolean("enabled", false).apply()
                val intent = Intent(this, WordWatchService::class.java)
                stopService(intent)
                enableBtn.text = "Enable Lock Screen Words"
                Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.newWordBtn).setOnClickListener {
            val word = WordProvider.getRandomWord(this)
            findViewById<TextView>(R.id.wordText).text = word.word
            findViewById<TextView>(R.id.meaningText).text = word.meaning
            findViewById<TextView>(R.id.meaningNPText).text = word.meaningNP
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perms = mutableListOf(Manifest.permission.POST_NOTIFICATIONS)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, perms.toTypedArray(), 100)
                return
            }
        }
        startService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            startService()
        }
    }

    private fun startService() {
        lockPref.edit().putBoolean("enabled", true).apply()
        val intent = Intent(this, WordWatchService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        findViewById<Button>(R.id.startOverlayBtn).text = "Disable"
        Toast.makeText(this, "Active! Lock your phone to see words.", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        val dw = WordProvider.getDailyWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP
        val isEnabled = lockPref.getBoolean("enabled", false)
        findViewById<Button>(R.id.startOverlayBtn).text = if (isEnabled) "Disable" else "Enable Lock Screen Words"
    }
}
