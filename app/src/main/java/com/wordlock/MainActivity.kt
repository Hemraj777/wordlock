package com.wordlock

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            startWordService()
        } else {
            Toast.makeText(this, "Notification permission required for WordLock to work", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshWord()

        findViewById<Button>(R.id.startOverlayBtn).setOnClickListener {
            checkPermissionsAndStart()
        }

        findViewById<Button>(R.id.addWidgetBtn).setOnClickListener {
            hintAddWidget()
        }

        findViewById<Button>(R.id.newWordBtn).setOnClickListener {
            refreshWord()
        }

        updateStatusText()
    }

    private fun checkPermissionsAndStart() {
        val permsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permsNeeded.toTypedArray())
        } else {
            startWordService()
        }
    }

    private fun startWordService() {
        val serviceIntent = Intent(this, WordNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Toast.makeText(this, "WordLock enabled!", Toast.LENGTH_SHORT).show()
        updateStatusText()
    }

    private fun hintAddWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val widgetProvider = ComponentName(this, WordWidgetProvider::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(widgetProvider, null, null)
            Toast.makeText(this, "Pin WordLock widget to your home screen", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this,
                "Long-press home screen \u2192 Widgets \u2192 WordLock",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateStatusText() {
        val serviceRunning = isServiceRunning(WordNotificationService::class.java.name)
        val statusText = findViewById<TextView>(R.id.statusText)
        if (serviceRunning) {
            statusText.text = "Active \u2014 new word on each screen unlock"
            statusText.setTextColor(0xFF4CAF50.toInt())
        } else {
            statusText.text = "Tap the button below to enable"
            statusText.setTextColor(0xFF9CA3AF.toInt())
        }
    }

    private fun isServiceRunning(serviceName: String): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceName == service.service.className) return true
        }
        return false
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
        updateStatusText()
    }
}
