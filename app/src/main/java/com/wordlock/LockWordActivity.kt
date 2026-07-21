package com.wordlock

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LockWordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val word = WordProvider.getRandomWord(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(60, 100, 60, 100)
            setBackgroundColor(Color.parseColor("#0F0F1A"))
        }

        val categoryTv = TextView(this).apply {
            text = "  ${word.category.uppercase()}  "
            setTextColor(Color.parseColor("#A855F7"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(24, 8, 24, 8)
            setBackgroundColor(Color.parseColor("#2D1B69"))
            gravity = Gravity.CENTER
        }
        layout.addView(categoryTv)

        val wordTv = TextView(this).apply {
            text = word.word
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 48f)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 48, 0, 8)
        }
        layout.addView(wordTv)

        val pronTv = TextView(this).apply {
            text = word.pronunciation
            setTextColor(Color.parseColor("#9CA3AF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
        }
        layout.addView(pronTv)

        val divider = View(this).apply {
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(120, 44, 120, 44)
            }
            layoutParams = lp
            setBackgroundColor(Color.parseColor("#1E1E2E"))
        }
        layout.addView(divider)

        val meaningTv = TextView(this).apply {
            text = word.meaning
            setTextColor(Color.parseColor("#D1D5DB"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            gravity = Gravity.CENTER
            setPadding(20, 0, 20, 0)
        }
        layout.addView(meaningTv)

        val meaningNpTv = TextView(this).apply {
            text = word.meaningNP
            setTextColor(Color.parseColor("#C084FC"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(20, 14, 20, 0)
        }
        layout.addView(meaningNpTv)

        val exampleTv = TextView(this).apply {
            text = word.example
            setTextColor(Color.parseColor("#6B7280"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            setPadding(40, 32, 40, 0)
        }
        layout.addView(exampleTv)

        val hintTv = TextView(this).apply {
            text = "Tap to dismiss"
            setTextColor(Color.parseColor("#3B3B4F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity = Gravity.CENTER
            setPadding(0, 64, 0, 0)
        }
        layout.addView(hintTv)

        setContentView(layout)

        val animSet = AnimationSet(true).apply {
            addAnimation(TranslateAnimation(0f, 0f, 300f, 0f).apply { duration = 500 })
            addAnimation(AlphaAnimation(0f, 1f).apply { duration = 500 })
        }
        layout.startAnimation(animSet)

        layout.setOnClickListener { finish() }
    }

    override fun onBackPressed() { finish() }
}
