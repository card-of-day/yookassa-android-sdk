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

package ru.yoomoney.sdk.kassa.payments.errorFormatter

import android.content.ActivityNotFoundException
import android.content.Context
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.model.ApiMethodException
import ru.yoomoney.sdk.kassa.payments.model.NoInternetException
import ru.yoomoney.sdk.kassa.payments.model.PassphraseCheckFailedException
import ru.yoomoney.sdk.kassa.payments.model.ErrorCode

internal open class DefaultErrorFormatter(private val context: Context): ErrorFormatter {
    override fun format(e: Throwable): CharSequence {
        return when (e) {
            is NoInternetException -> context.getText(R.string.ym_error_no_internet)
            is PassphraseCheckFailedException -> context.getText(R.string.ym_wrong_passcode_error)
            is ApiMethodException -> formatApiMethodException(e)
            is ActivityNotFoundException -> context.getText(R.string.ym_sbp_bank_not_found_message)
            else -> context.getText(R.string.ym_error_something_went_wrong)
        }
    }

    private fun formatApiMethodException(e: ApiMethodException): CharSequence {
        return when (e.error.errorCode) {
            ErrorCode.TECHNICAL_ERROR -> context.getText(R.string.ym_server_error)
            ErrorCode.UNKNOWN -> context.getText(R.string.ym_unknown_error)
            else -> context.getText(R.string.ym_error_something_went_wrong)
        }
    }
}