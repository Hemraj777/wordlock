package com.wordlock

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class WordOverlayService : Service() {

    private var screenReceiver: BroadcastReceiver? = null
    private var unlockReceiver: BroadcastReceiver? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentOverlay: View? = null
    private var wm: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WordLock Active")
            .setContentText("New word on each screen unlock")
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        registerReceivers()

        val hasPerm = Settings.canDrawOverlays(this)
        Log.d("WordLock", "Service created. Overlay permission: $hasPerm")
        handler.post {
            Toast.makeText(this, "WordLock service started. Overlay permission: $hasPerm", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerReceivers() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    val hasPerm = Settings.canDrawOverlays(context)
                    Log.d("WordLock", "SCREEN_ON fired. Overlay permission: $hasPerm")
                    if (!hasPerm) {
                        handler.post {
                            Toast.makeText(context, "WordLock: No overlay permission!", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                    handler.post {
                        try {
                            showOverlay()
                            Log.d("WordLock", "Overlay shown successfully")
                        } catch (e: Exception) {
                            Log.e("WordLock", "Failed to show overlay: ${e.message}")
                            Toast.makeText(context, "WordLock overlay error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_USER_PRESENT) {
                    Log.d("WordLock", "USER_PRESENT fired, removing overlay")
                    handler.post { removeOverlay() }
                }
            }
        }

        val screenFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        val unlockFilter = IntentFilter(Intent.ACTION_USER_PRESENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, screenFilter, Context.RECEIVER_NOT_EXPORTED)
            registerReceiver(unlockReceiver, unlockFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, screenFilter)
            registerReceiver(unlockReceiver, unlockFilter)
        }
    }

    private fun showOverlay() {
        removeOverlay()

        val word = WordProvider.getRandomWord(this)
        val screenWidth = resources.displayMetrics.widthPixels
        val cardWidth = (screenWidth * 0.82).toInt()

        val card = createPaperCard(word, cardWidth)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            cardWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = -500
        }

        currentOverlay = card
        wm?.addView(card, params)
        Log.d("WordLock", "View added to WindowManager for: ${word.word}")

        card.post {
            val cardHeight = card.height
            val displayHeight = resources.displayMetrics.heightPixels
            val targetY = (displayHeight - cardHeight) / 2

            val fallAnim = ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, -cardHeight.toFloat() - 100f, targetY.toFloat())
            fallAnim.duration = 1200
            fallAnim.interpolator = DecelerateInterpolator(1.5f)
            fallAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    handler.postDelayed({ removeOverlay() }, 5000)
                }
            })
            fallAnim.start()
            Log.d("WordLock", "Animation started")
        }

        card.setOnClickListener { removeOverlay() }
    }

    private fun createPaperCard(word: Word, width: Int): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(20))
            background = GradientDrawable().apply {
                setColor(0xFFF5F0E8.toInt())
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), 0xFFD1D5DB.toInt())
            }
        }

        val decorBar = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(50), dp(3)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(14)
            }
            background = GradientDrawable().apply {
                setColor(0xFFA855F7.toInt())
                cornerRadius = dp(2).toFloat()
            }
        }
        card.addView(decorBar)

        val wordText = TextView(this).apply {
            text = word.word
            setTextColor(0xFF1A1A2E.toInt())
            textSize = 22f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(4) }
        }
        card.addView(wordText)

        val pronText = TextView(this).apply {
            text = word.pronunciation
            setTextColor(0xFF6B7280.toInt())
            textSize = 11f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }
        card.addView(pronText)

        val divider1 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).apply { bottomMargin = dp(12) }
            setBackgroundColor(0xFFD1D5DB.toInt())
        }
        card.addView(divider1)

        val meaningText = TextView(this).apply {
            text = word.meaning
            setTextColor(0xFF374151.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }
        card.addView(meaningText)

        val meaningNPText = TextView(this).apply {
            text = word.meaningNP
            setTextColor(0xFF7C3AED.toInt())
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }
        card.addView(meaningNPText)

        val divider2 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).apply { bottomMargin = dp(12) }
            setBackgroundColor(0xFFD1D5DB.toInt())
        }
        card.addView(divider2)

        val exampleText = TextView(this).apply {
            text = "\u201C${word.example}\u201D"
            setTextColor(0xFF6B7280.toInt())
            textSize = 11f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            gravity = Gravity.CENTER
            setPadding(dp(12), 0, dp(12), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
        }
        card.addView(exampleText)

        val dismissText = TextView(this).apply {
            text = "tap to dismiss"
            setTextColor(0xFF9CA3AF.toInt())
            textSize = 10f
            gravity = Gravity.CENTER
        }
        card.addView(dismissText)

        return card
    }

    private fun removeOverlay() {
        currentOverlay?.let {
            try {
                wm?.removeView(it)
                Log.d("WordLock", "Overlay removed")
            } catch (e: Exception) {
                Log.e("WordLock", "Error removing overlay: ${e.message}")
            }
            currentOverlay = null
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WordLock",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps WordLock overlay running"
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        screenReceiver?.let { try { unregisterReceiver(it) } catch (_: Exception) {} }
        unlockReceiver?.let { try { unregisterReceiver(it) } catch (_: Exception) {} }
    }

    companion object {
        const val CHANNEL_ID = "wordlock_overlay"
        const val NOTIFICATION_ID = 8888
    }
}
