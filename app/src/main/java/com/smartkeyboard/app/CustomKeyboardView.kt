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

/**
 * Gboard-style renderer for the classic android.inputmethodservice.KeyboardView.
 * Stock KeyboardView can't color individual keys differently, so we take over
 * onDraw() completely: white rounded keys for letters/numbers, light-gray rounded
 * keys for function keys (shift, backspace, ?123, emoji, enter), a soft drop
 * shadow under every key, and a small superscript number hint (from
 * popupCharacters) on the top row.
 */
class CustomKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = android.R.attr.keyboardViewStyle
) : KeyboardView(context, attrs, defStyle) {

    private val density = context.resources.displayMetrics.density
    private val cornerRadius = 6f * density
    private val keyGap = 2f * density

    private val normalBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
    }
    private val functionBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E3E5E8")
    }
    private val pressedNormalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DADDE1")
    }
    private val pressedFunctionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C7C9CD")
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A000000")
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1E")
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

            // Space bar spans wide and looks best flat/blended rather than boxed
            val isSpace = key.codes.isNotEmpty() && key.codes[0] == 32

            if (!isSpace) {
                // soft drop shadow
                rect.set(left, top + 1.2f * density, right, bottom + 1.2f * density)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, shadowPaint)
            }

            rect.set(left, top, right, bottom)
            val bgPaint = when {
                isSpace -> if (key.pressed) pressedNormalPaint else normalBgPaint
                isFunction -> if (key.pressed) pressedFunctionPaint else functionBgPaint
                else -> if (key.pressed) pressedNormalPaint else normalBgPaint
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

            key.label?.let { label ->
                labelPaint.textSize = (if (isFunction) 17f else 19f) * density
                val textY = rect.centerY() - (labelPaint.descent() + labelPaint.ascent()) / 2f
                canvas.drawText(label.toString(), rect.centerX(), textY, labelPaint)
            }

            // superscript number hint on letter keys (from popupCharacters)
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
