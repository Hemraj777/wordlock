package com.wordlock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import java.util.Calendar

class WordWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = WordEngine()

    inner class WordEngine : Engine() {

        private var width = 0
        private var height = 0
        private val handler = Handler(Looper.getMainLooper())
        private var lastWordDay = -1

        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 56f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        private val pronPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 28f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }

        private val categoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A855F7")
            textSize = 20f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        private val meaningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB")
            textSize = 32f
            textAlign = Paint.Align.CENTER
            setLetterSpacing(0.02f)
        }

        private val meaningNPPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C084FC")
            textSize = 30f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        private val examplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6B7280")
            textSize = 24f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }

        private val bgPaint = Paint().apply {
            color = Color.parseColor("#0F0F1A")
        }

        private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E1E2E")
            strokeWidth = 1f
        }

        private val wordProvider by lazy { WordProvider }

        private val drawRunner = Runnable { drawFrame() }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                handler.removeCallbacks(drawRunner)
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            lastWordDay = -1
            handler.removeCallbacks(drawRunner)
            handler.post(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            width = w
            height = h
            updatePaintSizes()
            handler.removeCallbacks(drawRunner)
            handler.post(drawRunner)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        override fun onTouchEvent(event: MotionEvent) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                handler.removeCallbacks(drawRunner)
                handler.post(drawRunner)
            }
        }

        private fun updatePaintSizes() {
            val scale = width / 1080f
            titlePaint.textSize = 72f * scale
            pronPaint.textSize = 30f * scale
            categoryPaint.textSize = 22f * scale
            meaningPaint.textSize = 36f * scale
            meaningNPPaint.textSize = 34f * scale
            examplePaint.textSize = 28f * scale
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.let { drawOnCanvas(it) }
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }

        private fun drawOnCanvas(canvas: Canvas) {
            val w = canvas.width
            val h = canvas.height

            // Background
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            if (dayOfYear != lastWordDay) {
                lastWordDay = dayOfYear
            }

            val word = wordProvider.getDailyWord(this@WordWallpaperService)

            val centerX = w / 2f
            var y = h * 0.28f

            // Category badge
            val categoryText = "  ${word.category.uppercase()}  "
            val catW = categoryPaint.measureText(categoryText)
            val catBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6d28d9")
            }
            canvas.drawRoundRect(centerX - catW / 2 - 16, y - 28, centerX + catW / 2 + 16, y + 8, 20f, 20f, catBg)
            canvas.drawText(categoryText, centerX, y, categoryPaint)

            y += 70f

            // Word
            canvas.drawText(word.word, centerX, y, titlePaint)
            y += 55f

            // Pronunciation
            canvas.drawText(word.pronunciation, centerX, y, pronPaint)
            y += 80f

            // Divider
            canvas.drawLine(w * 0.15f, y, w * 0.85f, y, dividerPaint)
            y += 50f

            // Meaning (English) — wrap text
            y = drawWrappedText(canvas, word.meaning, centerX, y, w * 0.82f, meaningPaint)
            y += 20f

            // Meaning (Nepali)
            y = drawWrappedText(canvas, word.meaningNP, centerX, y, w * 0.82f, meaningNPPaint)
            y += 40f

            // Example
            val exampleText = word.example
            y = drawWrappedText(canvas, exampleText, centerX, y, w * 0.76f, examplePaint)

            // Timestamp at bottom
            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3B3B4F")
                textSize = 22f
                textAlign = Paint.Align.CENTER
            }
            val cal = Calendar.getInstance()
            val timeStr = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            canvas.drawText("WordLock  ·  $timeStr", centerX, h - 60f, timePaint)
        }

        private fun drawWrappedText(canvas: Canvas, text: String, centerX: Float, startY: Float, maxWidth: Float, paint: Paint): Float {
            val words = text.split(" ")
            val line = StringBuilder()
            var y = startY
            val lineHeight = paint.textSize * 1.4f

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth && line.isNotEmpty()) {
                    canvas.drawText(line.toString(), centerX, y, paint)
                    line.clear()
                    line.append(word)
                    y += lineHeight
                } else {
                    line.clear()
                    line.append(testLine)
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line.toString(), centerX, y, paint)
                y += lineHeight
            }
            return y
        }
    }
}
