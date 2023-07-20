/*
 * The MIT License (MIT)
 * Copyright © 2023 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.di.module

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.yoomoney.sdk.kassa.payments.api.SBPApi
import ru.yoomoney.sdk.kassa.payments.confirmation.SBPConfirmationBusinessLogic
import ru.yoomoney.sdk.kassa.payments.confirmation.SBPConfirmationContract
import ru.yoomoney.sdk.kassa.payments.confirmation.SBPConfirmationInfoGateway
import ru.yoomoney.sdk.kassa.payments.confirmation.SBPConfirmationUseCase
import ru.yoomoney.sdk.kassa.payments.confirmation.SBPConfirmationUseCaseImpl
import ru.yoomoney.sdk.kassa.payments.di.scope.ConfirmationScope
import ru.yoomoney.sdk.kassa.payments.di.ViewModelKey
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.input

@Module
internal class PaymentDetailsModule {

    @ConfirmationScope
    @Provides
    fun paymentDetailsInfoGatewayProvider(
        sbpApi: SBPApi
    ): SBPConfirmationInfoGateway {
        return SBPConfirmationInfoGateway(sbpApi)
    }

    @ConfirmationScope
    @Provides
    fun paymentDetailsUseCaseProvider(
        sbpConfirmationInfoGateway: SBPConfirmationInfoGateway,
    ): SBPConfirmationUseCase {
        return SBPConfirmationUseCaseImpl(sbpConfirmationInfoGateway)
    }

    @[Provides IntoMap ViewModelKey(PAYMENT_DETAILS)]
    fun viewModel(
        sbpConfirmationUseCase: SBPConfirmationUseCase,
    ): ViewModel {
        return RuntimeViewModel<SBPConfirmationContract.State, SBPConfirmationContract.Action, SBPConfirmationContract.Effect>(
            featureName = PAYMENT_DETAILS,
            initial = {
                Out(SBPConfirmationContract.State.Initial) {
                    input { showState(state) }
                }
            },
            logic = {
                SBPConfirmationBusinessLogic(
                    showState = showState,
                    showEffect = showEffect,
                    source = source,
                    paymentDetailsUseCase = sbpConfirmationUseCase
                )
            }
        )
    }

    companion object {
        const val PAYMENT_DETAILS = "PaymentDetails"
    }
}