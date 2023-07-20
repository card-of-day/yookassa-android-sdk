/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yoomoney.sdk.kassa.payments.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import kotlinx.android.synthetic.main.ym_view_error.view.*
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository

internal class ErrorView : LinearLayoutCompat {

    constructor(context: Context) : this(context, attrs = null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        View.inflate(context, R.layout.ym_view_error, this)
        orientation = VERTICAL
        setAttributes(context, attrs, defStyleAttr)
    }

    fun setErrorText(text: CharSequence) {
        message.text = text
        message.visibility = View.VISIBLE
    }

    fun setErrorButtonText(text: CharSequence) {
        button.text = text
        button.visibility = View.VISIBLE
    }

    fun setErrorButtonListener(listener: OnClickListener) {
        button.setOnClickListener(listener)
        button.setTextColor(InMemoryColorSchemeRepository.typeColorStateList(context))
    }

    fun setErrorButtonListener(action: () -> Unit) {
        button.setOnClickListener { action() }
        button.setTextColor(InMemoryColorSchemeRepository.typeColorStateList(context))
    }

    private fun setAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.ym_ErrorView, defStyleAttr, 0)

        attributes.getText(R.styleable.ym_ErrorView_ym_error_text)?.apply {
            setErrorText(this)
        }

        attributes.getText(R.styleable.ym_ErrorView_ym_error_button_text)?.apply {
            setErrorButtonText(this)
        }

        attributes.recycle()
    }
}
