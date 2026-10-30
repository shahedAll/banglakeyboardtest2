package com.shahed.braumono

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo

class BrauKeyboardService : InputMethodService() {
    private lateinit var keyboard: BrauKeyboardView
    override fun onCreateInputView(): View {
        keyboard = BrauKeyboardView(this) { key ->
            val ic = currentInputConnection ?: return@BrauKeyboardView
            when (key) {
                "⌫" -> ic.deleteSurroundingText(1, 0)
                "↵" -> {
                    val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                    if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                        ic.performEditorAction(action)
                    } else ic.commitText("\n", 1)
                }
                "SPACE" -> ic.commitText(" ", 1)
                "⇧" -> keyboard.toggleShift()
                "123" -> keyboard.toggleSymbols()
                "ABC" -> keyboard.toggleSymbols()
                else -> ic.commitText(keyboard.outputFor(key), 1)
            }
        }
        return keyboard
    }
}
