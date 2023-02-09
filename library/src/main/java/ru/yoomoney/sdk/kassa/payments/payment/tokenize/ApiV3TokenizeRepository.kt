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

package ru.yoomoney.sdk.kassa.payments.payment.tokenize

import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.extensions.CheckoutOkHttpClient
import ru.yoomoney.sdk.kassa.payments.extensions.execute
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.methods.InstrumentTokenRequest
import ru.yoomoney.sdk.kassa.payments.methods.TokenRequest
import ru.yoomoney.sdk.kassa.payments.model.Confirmation
import ru.yoomoney.sdk.kassa.payments.model.PaymentInstrumentBankCard
import ru.yoomoney.sdk.kassa.payments.model.PaymentOption
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionInfo
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthTokenRepository
import ru.yoomoney.sdk.kassa.payments.tmx.ProfilingSessionIdStorage
import ru.yoomoney.sdk.yooprofiler.YooProfiler

internal class ApiV3TokenizeRepository(
    private val hostProvider: HostProvider,
    private val httpClient: Lazy<CheckoutOkHttpClient>,
    private val shopToken: String,
    private val paymentAuthTokenRepository: PaymentAuthTokenRepository,
    private val profilingSessionIdStorage: ProfilingSessionIdStorage,
    private val profiler: YooProfiler,
    private val merchantCustomerId: String?
) : TokenizeRepository {

    override fun getToken(
        paymentOption: PaymentOption,
        paymentOptionInfo: PaymentOptionInfo,
        savePaymentMethod: Boolean,
        savePaymentInstrument: Boolean,
        confirmation: Confirmation
    ): Result<String> {
        val currentProfilingSessionId = acquireProfilingSessionId() ?: return Result.Fail(TmxProfilingFailedException())
        val paymentAuthToken = paymentAuthTokenRepository.paymentAuthToken
        val tokenRequest = TokenRequest(
            hostProvider = hostProvider,
            paymentOptionInfo = paymentOptionInfo,
            paymentOption = paymentOption,
            tmxSessionId = currentProfilingSessionId,
            shopToken = shopToken,
            paymentAuthToken = paymentAuthToken,
            confirmation = confirmation,
            savePaymentMethod = savePaymentMethod,
            savePaymentInstrument = savePaymentInstrument,
            merchantCustomerId = merchantCustomerId
        )
        profilingSessionIdStorage.profilingSessionId = null
        return httpClient.value.execute(tokenRequest)
    }

    override fun getToken(
        instrumentBankCard: PaymentInstrumentBankCard,
        amount: Amount,
        savePaymentMethod: Boolean,
        csc: String?,
        confirmation: Confirmation
    ): Result<String> {
        val currentProfilingSessionId = acquireProfilingSessionId() ?: return Result.Fail(TmxProfilingFailedException())
        val paymentAuthToken = paymentAuthTokenRepository.paymentAuthToken
        val tokenRequest = InstrumentTokenRequest(
            hostProvider = hostProvider,
            amount = amount,
            tmxSessionId = currentProfilingSessionId,
            shopToken = shopToken,
            paymentAuthToken = paymentAuthToken,
            confirmation = confirmation,
            savePaymentMethod = savePaymentMethod,
            csc = csc,
            instrumentBankCard = instrumentBankCard
        )
        return httpClient.value.execute(tokenRequest)
    }

    private fun acquireProfilingSessionId(): String {
        val profilingSessionId = profilingSessionIdStorage.profilingSessionId
        if (profilingSessionId.isNullOrEmpty()) {
            return when (val result = profiler.profile()) {
                is YooProfiler.Result.Success -> result.sessionId
                is YooProfiler.Result.Fail -> result.description
            }
        }
        return profilingSessionId
    }
}

internal class TmxProfilingFailedException : Exception()
