package com.wordlock

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
        updateButton(enableBtn)

        enableBtn.setOnClickListener {
            val isEnabled = lockPref.getBoolean("enabled", false)
            if (!isEnabled) {
                requestAllPermissions()
            } else {
                disableService(enableBtn)
            }
        }

        findViewById<Button>(R.id.newWordBtn).setOnClickListener {
            val word = WordProvider.getRandomWord(this)
            findViewById<TextView>(R.id.wordText).text = word.word
            findViewById<TextView>(R.id.meaningText).text = word.meaning
            findViewById<TextView>(R.id.meaningNPText).text = word.meaningNP
        }
    }

    private fun requestAllPermissions() {
        val permsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permsNeeded.toTypedArray(), 100)
        } else {
            checkFullScreenIntent()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            checkFullScreenIntent()
        }
    }

    private fun checkFullScreenIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.canUseFullScreenIntent()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
                Toast.makeText(this, "Please allow 'Full screen notifications', then come back and enable", Toast.LENGTH_LONG).show()
                return
            }
        }
        enableService()
    }

    private fun enableService() {
        lockPref.edit().putBoolean("enabled", true).apply()
        val intent = Intent(this, WordWatchService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateButton(findViewById(R.id.startOverlayBtn))
        Toast.makeText(this, "Active! New word every time you unlock.", Toast.LENGTH_LONG).show()
    }

    private fun disableService(btn: Button) {
        lockPref.edit().putBoolean("enabled", false).apply()
        stopService(Intent(this, WordWatchService::class.java))
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()
        updateButton(btn)
        Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show()
    }

    private fun updateButton(btn: Button) {
        val isEnabled = lockPref.getBoolean("enabled", false)
        btn.text = if (isEnabled) "Disable" else "Enable Lock Screen Words"
    }

    override fun onResume() {
        super.onResume()
        val dw = WordProvider.getDailyWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP
        updateButton(findViewById(R.id.startOverlayBtn))
    }
}
