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

package ru.yoomoney.sdk.kassa.payments.confirmation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.ym_confirmation_fragment.errorView
import kotlinx.android.synthetic.main.ym_confirmation_fragment.loadingView
import kotlinx.android.synthetic.main.ym_confirmation_fragment.rootContainer
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.di.module.PaymentDetailsModule
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.showChild
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject

internal typealias ConfirmationViewModel = RuntimeViewModel<SBPConfirmationContract.State, SBPConfirmationContract.Action, SBPConfirmationContract.Effect>

internal class ConfirmationFragment : Fragment(R.layout.ym_confirmation_fragment) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    private val viewModel: ConfirmationViewModel
            by viewModel(PaymentDetailsModule.PAYMENT_DETAILS) {
                viewModelFactory
            }

    @Inject
    lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.injectConfirmationFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observe(
            lifecycleOwner = this,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = {}
        )
        getConfirmationData()
    }

    private fun showState(state: SBPConfirmationContract.State) {
        when (state) {
            is SBPConfirmationContract.State.Loading -> rootContainer.showChild(loadingView)
            is SBPConfirmationContract.State.LoadingDataFailed -> {
                errorView.setErrorText(errorFormatter.format(state.throwable))
                errorView.setErrorButtonListener {
                    getConfirmationData()
                }
                rootContainer.showChild(errorView)
                loadingView.updateLayoutParams<ViewGroup.LayoutParams> { height = rootContainer.getViewHeight() }
            }
            else -> Unit
        }
    }

    private fun showEffect(effect: SBPConfirmationContract.Effect) {
        when (effect) {
            is SBPConfirmationContract.Effect.ContinueSBPConfirmation -> {
                router.navigateTo(Screen.BankList(effect.confirmationUrl, effect.paymentId))
            }
            is SBPConfirmationContract.Effect.SendFinishState -> {
                router.navigateTo(Screen.SBPConfirmationSuccessful)
            }
        }
    }

    private fun getConfirmationData() {
        viewModel.handleAction(
            SBPConfirmationContract.Action.GetConfirmationDetails(
                requireNotNull(
                    arguments?.getString(
                        CONFIRMATION_DATA
                    )
                )
            )
        )
    }

    companion object {
        private const val CONFIRMATION_DATA = "ru.yoomoney.sdk.kassa.payments.confirmation.CONFIRMATION_DATA"

        fun createFragment(confirmationData: String): ConfirmationFragment {
            return ConfirmationFragment().apply {
                arguments = Bundle().apply {
                    putString(CONFIRMATION_DATA, confirmationData)
                }
            }
        }

    }
}