package com.smartkeyboard.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

class CustomKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0
) : KeyboardView(context, attrs, defStyle) {

    private val density = context.resources.displayMetrics.density
    private val cornerRadius = 8f * density
    private val keyGap = 3f * density

    // Floris-style flat palette — no per-key shadow bitmap/layer, just flat rounded rects (cheap to draw)
    private val normalBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
    }
    private val functionBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E4E7ED")
    }
    private val pressedNormalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D7DBE2")
    }
    private val pressedFunctionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C4C9D1")
    }
    private val shiftActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4C6EF5")
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1E")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val shiftLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9AA0A6")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
        textSize = 10f * density
    }

    private val rect = RectF()

    private fun isFunctionKey(key: Keyboard.Key): Boolean {
        if (key.codes.isNotEmpty() && key.codes[0] < 0) return true
        val label = key.label?.toString()
        return label == "," || label == "."
    }

    override fun onDraw(canvas: Canvas) {
        val kb = keyboard ?: return
        val padLeft = paddingLeft.toFloat()
        val padTop = paddingTop.toFloat()

        for (key in kb.keys) {
            val left = padLeft + key.x + keyGap / 2f
            val top = padTop + key.y + keyGap / 2f
            val right = padLeft + key.x + key.width - keyGap / 2f
            val bottom = padTop + key.y + key.height - keyGap / 2f

            val isFunction = isFunctionKey(key)
            val isShiftKey = key.codes.isNotEmpty() && key.codes[0] == -1
            val isShiftOn = isShiftKey && kb.isShifted

            // Single flat rounded-rect per key — no shadow layer, keeps drawing cheap/fast
            rect.set(left, top, right, bottom)
            val bgPaint = when {
                isShiftOn -> shiftActivePaint
                isFunction -> if (key.pressed) pressedFunctionPaint else functionBgPaint
                else -> if (key.pressed) pressedNormalPaint else normalBgPaint
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

            key.label?.let { label ->
                val paint = if (isShiftOn) shiftLabelPaint else labelPaint
                paint.textSize = (if (isFunction) 15f else 17f) * density
                var text = label.toString()
                if (kb.isShifted && text.length == 1 && text[0].isLetter()) {
                    text = text.uppercase()
                }
                val textY = rect.centerY() - (paint.descent() + paint.ascent()) / 2f
                canvas.drawText(text, rect.centerX(), textY, paint)
            }

            val hint = key.popupCharacters
            val lbl = key.label
            if (!hint.isNullOrEmpty() && hint.length == 1 && lbl != null && lbl.length == 1 && lbl[0].isLetter()) {
                canvas.drawText(
                    hint.toString(),
                    rect.right - 10f * density,
                    rect.top + 13f * density,
                    hintPaint
                )
            }
        }
    }
}
