package com.wordlock

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

        findViewById<Button>(R.id.newWordBtn).setOnClickListener {
            WordWidgetProvider.updateAllWidgets(this)
            refreshWord()
            Toast.makeText(this, "Widget updated!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.startOverlayBtn).setOnClickListener {
            Toast.makeText(this, "Long-press home screen → Widgets → WordLock", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshWord() {
        val dw = WordProvider.getDailyWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP
    }

    override fun onResume() {
        super.onResume()
        refreshWord()
    }
}
