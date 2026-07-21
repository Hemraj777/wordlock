package com.wordlock

import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dw = WordProvider.getDailyWord(this)
        findViewById<TextView>(R.id.wordText).text = dw.word
        findViewById<TextView>(R.id.meaningText).text = dw.meaning
        findViewById<TextView>(R.id.meaningNPText).text = dw.meaningNP

        findViewById<Button>(R.id.startOverlayBtn).setOnClickListener {
            try {
                val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_PICKER)
                startActivity(intent)
                Toast.makeText(this, "Select WordLock wallpaper", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Fallback: open wallpaper settings
                try {
                    val intent = Intent("android.service.wallpaper.WallpaperService")
                    startActivity(intent)
                } catch (e2: Exception) {
                    Toast.makeText(this, "Go to Settings → Display → Wallpaper → Live Wallpapers → WordLock", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<Button>(R.id.newWordBtn).setOnClickListener {
            val word = WordProvider.getRandomWord(this)
            findViewById<TextView>(R.id.wordText).text = word.word
            findViewById<TextView>(R.id.meaningText).text = word.meaning
            findViewById<TextView>(R.id.meaningNPText).text = word.meaningNP
            Toast.makeText(this, "New word! (Set as wallpaper to see it there)", Toast.LENGTH_SHORT).show()
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
