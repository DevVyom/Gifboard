package com.vyom.moderngifboard.ime

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class GifKeyboardService : InputMethodService() {
    override fun onCreateInputView(): View {
        val density = resources.displayMetrics.density
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            minimumHeight = (300 * density).toInt()
            addView(TextView(this@GifKeyboardService).apply {
                text = "ModernGifBoard is running"
                textSize = 22f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(24, 48, 24, 48)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (300 * density).toInt()
            ))
        }
    }
}
