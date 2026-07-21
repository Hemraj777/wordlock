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

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val word = WordProvider.getRandomWord(context)
            val views = RemoteViews(context.packageName, R.layout.widget_word).apply {
                setTextViewText(R.id.widgetWord, word.word)
                setTextViewText(R.id.widgetPron, word.pronunciation)
                setTextViewText(R.id.widgetCategory, word.category.uppercase())
                setTextViewText(R.id.widgetMeaning, word.meaning)
                setTextViewText(R.id.widgetMeaningNP, word.meaningNP)
            }
            manager.updateAppWidget(id, views)
        }
    }
}
