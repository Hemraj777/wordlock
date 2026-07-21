package com.wordlock

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LockWordActivity : AppCompatActivity() {

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                finish()
            }
        }
    }

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
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val word = WordProvider.getRandomWord(this)

        val container = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#0F0F1A"))
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(60, 80, 60, 80)
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
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 40, 0, 8)
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
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2
            ).apply {
                setMargins(120, 40, 120, 40)
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
            setPadding(20, 12, 20, 0)
        }
        layout.addView(meaningNpTv)

        val exampleTv = TextView(this).apply {
            text = word.example
            setTextColor(Color.parseColor("#6B7280"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            setPadding(40, 28, 40, 0)
        }
        layout.addView(exampleTv)

        val hintTv = TextView(this).apply {
            text = "Tap to dismiss"
            setTextColor(Color.parseColor("#3B3B4F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity = Gravity.CENTER
            setPadding(0, 60, 0, 0)
        }
        layout.addView(hintTv)

        container.addView(layout)
        setContentView(container)

        val animSet = AnimationSet(true).apply {
            addAnimation(TranslateAnimation(0f, 0f, 200f, 0f).apply { duration = 400 })
            addAnimation(AlphaAnimation(0f, 1f).apply { duration = 400 })
        }
        layout.startAnimation(animSet)

        container.setOnClickListener { finish() }

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    override fun onDestroy() {
        try { unregisterReceiver(screenOffReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBackPressed() { finish() }
}
