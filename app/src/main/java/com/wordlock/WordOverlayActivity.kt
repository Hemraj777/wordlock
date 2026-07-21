package com.wordlock

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WordOverlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguard = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguard.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_overlay)

        val word = WordProvider.getRandomWord(this)

        findViewById<TextView>(R.id.overlayCategory).text = word.category.uppercase()
        findViewById<TextView>(R.id.overlayWord).text = word.word
        findViewById<TextView>(R.id.overlayPron).text = word.pronunciation
        findViewById<TextView>(R.id.overlayMeaning).text = word.meaning
        findViewById<TextView>(R.id.overlayMeaningNP).text = word.meaningNP

        findViewById<Button>(R.id.overlayDismiss).setOnClickListener {
            finish()
        }
    }
}
