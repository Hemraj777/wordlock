package com.wordlock

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class WordOverlayActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable { finish() }

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

        findViewById<TextView>(R.id.paperWord).text = word.word
        findViewById<TextView>(R.id.paperPron).text = word.pronunciation
        findViewById<TextView>(R.id.paperMeaning).text = word.meaning
        findViewById<TextView>(R.id.paperMeaningNP).text = word.meaningNP
        findViewById<TextView>(R.id.paperExample).text = "\"${word.example}\""

        val card = findViewById<CardView>(R.id.paperCard)

        card.post {
            val screenHeight = resources.displayMetrics.heightPixels.toFloat()
            val cardHeight = card.height.toFloat()

            card.translationY = -cardHeight - 100f

            val fallAnimator = ObjectAnimator.ofFloat(
                card, View.TRANSLATION_Y,
                -cardHeight - 100f, (screenHeight - cardHeight) / 2f
            )
            fallAnimator.duration = 1200
            fallAnimator.interpolator = DecelerateInterpolator(1.5f)

            fallAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    handler.postDelayed(autoDismissRunnable, 4000)
                }
            })

            fallAnimator.start()
        }

        card.setOnClickListener {
            handler.removeCallbacks(autoDismissRunnable)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoDismissRunnable)
    }
}
