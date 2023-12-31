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

package ru.yoomoney.sdk.kassa.payments.payment.tokenize

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.model.PaymentTokenInfo
import ru.yoomoney.sdk.kassa.payments.model.Result

internal interface TokenizeRepository {

    suspend fun getToken(
        amount: Amount,
        paymentOption: PaymentOption,
        paymentOptionInfo: PaymentOptionInfo,
        savePaymentMethod: Boolean,
        savePaymentInstrument: Boolean,
        confirmation: Confirmation
    ): Result<PaymentTokenInfo>

    suspend fun getToken(
        paymentOption: PaymentOption,
        instrumentBankCard: PaymentInstrumentBankCard,
        amount: Amount,
        savePaymentMethod: Boolean,
        csc: String?,
        confirmation: Confirmation
    ): Result<PaymentTokenInfo>

    suspend fun getToken(
        amount: Amount,
        paymentOption: PaymentOption,
        savePaymentMethod: Boolean,
        confirmation: Confirmation,
        paymentMethodId: String,
        csc: String?
    ): Result<PaymentTokenInfo>
}
