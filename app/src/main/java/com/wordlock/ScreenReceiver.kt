package com.wordlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                WordWidgetProvider.updateAllWidgets(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                WordWidgetProvider.updateAllWidgets(context)
            }
        }
    }
}
