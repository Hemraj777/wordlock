package com.wordlock

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetProvider::class.java))
        for (id in ids) {
            updateWidget(context, manager, id)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            try {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetProvider::class.java))
                if (ids.isNotEmpty()) {
                    for (id in ids) {
                        updateWidget(context, manager, id)
                    }
                }
            } catch (_: Exception) {}
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            try {
                val word = WordProvider.getRandomWord(context)
                val views = RemoteViews(context.packageName, R.layout.widget_word)
                views.setTextViewText(R.id.widgetWord, word.word)
                views.setTextViewText(R.id.widgetPron, word.pronunciation)
                views.setTextViewText(R.id.widgetCategory, word.category.uppercase())
                views.setTextViewText(R.id.widgetMeaning, word.meaning)
                views.setTextViewText(R.id.widgetMeaningNP, word.meaningNP)
                manager.updateAppWidget(id, views)
            } catch (_: Exception) {}
        }
    }
}
