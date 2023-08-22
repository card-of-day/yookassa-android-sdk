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

package ru.yoomoney.sdk.kassa.payments.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

internal sealed class PaymentOptionInfo: Parcelable

@Parcelize
internal data class NewCardInfo(
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val csc: String
) : PaymentOptionInfo() {
    override fun toString() =
        "NewCardInfo(number='**** **** **** ${number.takeLast(4)}', expirationMonth='**', expirationYear='**', csc='***')"
}

@Parcelize
internal class WalletInfo : PaymentOptionInfo() {
    override fun equals(other: Any?) = when {
        this === other -> true
        other !is WalletInfo -> false
        else -> true
    }

    override fun hashCode() = javaClass.hashCode()

    override fun toString() = "WalletInfo()"
}

@Parcelize
internal data class LinkedCardInfo(val csc: String) : PaymentOptionInfo() {
    override fun toString() = "LinkedCardInfo(csc='***')"
}

@Parcelize
internal data class SbolSmsInvoicingInfo(val phone: String) : PaymentOptionInfo()

@Parcelize
internal object SberPay: PaymentOptionInfo()

@Parcelize
internal object SBPInfo: PaymentOptionInfo()

@Parcelize
internal data class GooglePayInfo(
    val paymentMethodToken: String,
    val googleTransactionId: String
) : PaymentOptionInfo()
