package com.wordlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                val lockPref = context.getSharedPreferences("wordlock", Context.MODE_PRIVATE)
                val enabled = lockPref.getBoolean("enabled", false)
                if (enabled) {
                    val activityIntent = Intent(context, LockWordActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    }
                    context.startActivity(activityIntent)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                val lockPref = context.getSharedPreferences("wordlock", Context.MODE_PRIVATE)
                lockPref.edit().putBoolean("enabled", true).apply()
                val svcIntent = Intent(context, WordWatchService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(svcIntent)
                } else {
                    context.startService(svcIntent)
                }
            }
        }
    }
}
