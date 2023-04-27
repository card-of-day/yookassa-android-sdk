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

package ru.yoomoney.sdk.kassa.payments.di

import android.content.Context
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.http.authPaymentsHttpClient
import ru.yoomoney.sdk.kassa.payments.http.newAuthorizedHttpClient
import ru.yoomoney.sdk.kassa.payments.http.newHttpClient
import ru.yoomoney.sdk.kassa.payments.secure.TokensStorage

@Module
internal open class OkHttpModule {
    @Provides
    open fun okHttpClient(context: Context, testParameters: TestParameters): OkHttpClient {
        return newHttpClient(context, testParameters.showLogs, testParameters.hostParameters.isDevHost)
    }

    @Provides
    @AuthorizedHttpClient
    open fun authorizedOkHttpClient(
        context: Context,
        testParameters: TestParameters,
        paymentParameters: PaymentParameters,
        tokensStorage: TokensStorage
    ): OkHttpClient {
        return newAuthorizedHttpClient(
            context = context,
            showLogs = testParameters.showLogs,
            isDevHost = testParameters.hostParameters.isDevHost,
            shopToken = paymentParameters.clientApplicationKey,
            tokensStorage  = tokensStorage
        )
    }

    @Provides
    @AuthPaymentsHttpClient
    open fun authPaymentsOkHttpClient(
        context: Context,
        testParameters: TestParameters,
        paymentParameters: PaymentParameters,
        tokensStorage: TokensStorage
    ): OkHttpClient {
        return authPaymentsHttpClient(
            context = context,
            showLogs = testParameters.showLogs,
            isDevHost = testParameters.hostParameters.isDevHost,
            shopToken = paymentParameters.clientApplicationKey,
            tokensStorage  = tokensStorage
        )
    }
}