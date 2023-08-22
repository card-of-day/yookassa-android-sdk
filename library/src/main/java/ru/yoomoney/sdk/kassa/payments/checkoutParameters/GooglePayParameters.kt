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

package ru.yoomoney.sdk.kassa.payments.checkoutParameters

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.GooglePayCardNetwork.AMEX
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.GooglePayCardNetwork.DISCOVER
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.GooglePayCardNetwork.JCB
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.GooglePayCardNetwork.MASTERCARD
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.GooglePayCardNetwork.VISA

/**
 * Settings for Google Pay payment method. This class is one of the parameters of [PaymentParameters].
 * @param allowedCardNetworks (optional) networks, that can be used by user. Google Pay will only show cards that belong
 * to this set. If no value is specified, the default set will be used.
 */
@[Parcelize Keep SuppressLint("ParcelCreator")]
data class GooglePayParameters
@[JvmOverloads Keep] constructor(
    @Keep val allowedCardNetworks: Set<GooglePayCardNetwork> = setOf(AMEX, DISCOVER, JCB, VISA, MASTERCARD)
) : Parcelable

@Keep
enum class GooglePayCardNetwork {
    @Keep AMEX,
    @Keep DISCOVER,
    @Keep JCB,
    @Keep MASTERCARD,
    @Keep VISA,
    @Keep INTERAC,
    @Keep OTHER
}
