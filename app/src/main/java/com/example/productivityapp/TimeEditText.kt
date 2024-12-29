package com.example.productivityapp

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class TimeEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private val timeFormat = "##:##"

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                // Remove any non-digit characters
                val digits = s?.filter { it.isDigit() } ?: ""
                var formatted = ""

                for (i in digits.indices) {
                    if (i < timeFormat.length) {
                        if (timeFormat[i] == ':' && formatted.length == i) {
                            formatted += ":"
                        }
                        formatted += digits[i]
                    }
                }

                s?.replace(0, s.length, formatted)
                isFormatting = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        filters = arrayOf(InputFilter.LengthFilter(5))
    }

    fun isValidTime(): Boolean {
        val time = text.toString()
        if (time.length != 5) return false

        val (hours, minutes) = time.split(":").map { it.toIntOrNull() ?: return false }
        return hours in 0..23 && minutes in 0..59
    }
}
