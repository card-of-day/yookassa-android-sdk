/*
 * The MIT License (MIT)
 * Copyright © 2022 NBCO YooMoney LLC
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

/*
 * The MIT License (MIT)
 * Copyright © 2022 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.model.mapper

import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentInstrumentYooMoney
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionResponse
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionBankCard
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionGooglePay
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.PaymentOptionSberbank
import ru.yoomoney.sdk.kassa.payments.api.model.packageoptions.Unknown
import ru.yoomoney.sdk.kassa.payments.model.ConfigPaymentOption

internal fun PaymentOptionResponse.map(id: Int, configPaymentOptions: List<ConfigPaymentOption>): ru.yoomoney.sdk.kassa.payments.model.PaymentOption? {
    return when(this) {
        is PaymentOptionBankCard -> this.map(id, configPaymentOptions.first { it.method == paymentMethodType.value })
        is PaymentInstrumentYooMoney -> this.map(id, configPaymentOptions)
        is PaymentOptionSberbank -> this.map(id, configPaymentOptions.first { it.method == paymentMethodType.value })
        is PaymentOptionGooglePay -> this.map(id, configPaymentOptions.first { it.method == paymentMethodType.value })
        is Unknown -> null
    }
}