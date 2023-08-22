/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.databinding.YmInformerViewBinding

internal class InformerView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: YmInformerViewBinding = YmInformerViewBinding.inflate(LayoutInflater.from(context), this, true)

    var messageText: CharSequence? = null
        set(value) {
            binding.message.text = value
            field = value
        }

    var actionText: CharSequence? = null
        set(value) {
            binding.action.text = value
            field = value
        }

    init {
        setAttributes(context, attrs, defStyleAttr)
    }

    fun setActionClickListener(listener: OnClickListener) {
        binding.action.setOnClickListener(listener)
    }

    private fun setAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.ym_InformerView, defStyleAttr, 0)

        attributes.getText(R.styleable.ym_InformerView_ym_action_text)?.apply {
            actionText = this
        }

        attributes.getText(R.styleable.ym_InformerView_ym_message_text)?.apply {
            messageText = this
        }

        attributes.recycle()
    }
}