package ru.otus.basicarchitecture

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


class MaskWatcher(private val mask: String, private val editText: EditText) : TextWatcher {

    private var isUpdating = false
    private var lastFormattedText = ""
    private var lastCursorPosition = 0

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        lastCursorPosition = start // Запоминаем позицию курсора перед изменением
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (isUpdating || s.isNullOrEmpty()) return

        val cleanText = s.filter { it.isDigit() } // Оставляем только цифры
        val formattedText = applyMask(cleanText.toString())

        if (formattedText == lastFormattedText) return

        isUpdating = true
        editText.setText(formattedText)

        // Корректируем позицию курсора
        val cursorPosition = calculateCursorPosition(start, before, count, formattedText, cleanText.length)
        editText.setSelection(cursorPosition.coerceIn(0, formattedText.length)) // Защита от выхода за границы
        lastFormattedText = formattedText
        isUpdating = false
    }

    override fun afterTextChanged(s: Editable?) {}

    private fun applyMask(cleanText: String): String {
        val formattedText = StringBuilder()
        var cleanIndex = 0
        var maskIndex = 0

        while (maskIndex < mask.length) {
            if (mask[maskIndex] == '#') {
                if (cleanIndex < cleanText.length) {
                    formattedText.append(cleanText[cleanIndex])
                    cleanIndex++
                } else {
                    formattedText.append('_') // Use underscore for empty mask positions
                }
            } else {
                formattedText.append(mask[maskIndex])
            }
            maskIndex++
        }

        return formattedText.toString()
    }

    private fun calculateCursorPosition(start: Int, before: Int, count: Int, formattedText: String, cleanLength: Int): Int {
        var pos = start + count // Базовое смещение курсора

        if (count > before) {
            // New character entered
            var offset = 0
            var maskIndex = 0
            var cleanIndex = 0

            while (cleanIndex < cleanLength && maskIndex < mask.length) {
                if (mask[maskIndex] == '#') {
                    if (cleanIndex == start) {
                        pos += offset
                        break
                    }
                    cleanIndex++
                }
                if (mask[maskIndex] != '#') offset++
                maskIndex++
            }
        } else if (before > count) {
            // Character was deleted
            while (pos > 0 && formattedText.getOrNull(pos - 1) !in '0'..'9') {
                pos-- // Move cursor back if we're on a non-digit character
            }
            // If cursor is on an underscore or after the last digit, move to the nearest digit or start of string
            if (pos > 0 && formattedText[pos - 1] == '_') {
                while (pos > 0 && formattedText[pos - 1] == '_') {
                    pos--
                }
            }
        }

        // Ensure cursor is on a digit, underscore, or at the end of the string
        while (pos < formattedText.length && formattedText[pos] !in '0'..'9' && formattedText[pos] != '_') {
            pos++
        }

        return pos
    }
}