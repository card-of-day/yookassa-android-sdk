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

package ru.yoomoney.sdk.kassa.payments.ui

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.ym_fragment_bottom_sheet.topArrowLine
import ru.yoomoney.sdk.gui.utils.extensions.hide
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.confirmation.ConfirmationFragment
import ru.yoomoney.sdk.kassa.payments.confirmation.EXTRA_PAYMENT_METHOD_TYPE
import ru.yoomoney.sdk.kassa.payments.contract.ContractFragment
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.inTransaction
import ru.yoomoney.sdk.kassa.payments.metrics.SessionReporter
import ru.yoomoney.sdk.kassa.payments.model.LinkedCard
import ru.yoomoney.sdk.kassa.payments.model.toType
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.payment.GetLoadedPaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.payment.sbp.BankListFragment
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeFragment
import ru.yoomoney.sdk.kassa.payments.ui.model.StartScreenData
import ru.yoomoney.sdk.kassa.payments.ui.view.BackPressedAppCompatDialog
import ru.yoomoney.sdk.kassa.payments.ui.view.BackPressedBottomSheetDialog
import ru.yoomoney.sdk.kassa.payments.ui.view.WithBackPressedListener
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardFragment
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import javax.inject.Inject

private const val PAYMENT_OPTION_LIST_FRAGMENT_TAG = "paymentOptionListFragment"
private const val CONTRACT_FRAGMENT_TAG = "contractFragment"
private const val TOKENIZE_FRAGMENT_TAG = "tokenizeFragment"
private const val AUTH_FRAGMENT_TAG = "authFragment"
private const val PAYMENT_AUTH_FRAGMENT_TAG = "paymentAuthFragment"
private const val UNBIND_CARD_FRAGMENT_TAG = "unbindCardFragment"
private const val CONFIRMATION_SBP_FRAGMENT_TAG = "confirmationSBPFragment"
private const val LIST_BANKS_FRAGMENT_TAG = "listBanksFragment"

internal class MainDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var sessionReporter: SessionReporter

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var loadedPaymentOptionListRepository: GetLoadedPaymentOptionListRepository

    init {
        setStyle(STYLE_NO_FRAME, R.style.ym_MainDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = requireContext().let {
        CheckoutInjector.injectMainDialogFragment(this)
        if (isTablet) {
            BackPressedAppCompatDialog(it, theme).apply {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_PAN)
            }
        } else {
            BackPressedBottomSheetDialog(it, theme).apply {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE or SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ym_fragment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isTablet) {
            topArrowLine.hide()
        }

        val startScreenData = arguments?.getParcelable(START_SCREEN_DATA_INFO) as StartScreenData?
        val confirmationData = startScreenData?.confirmationData
        when {
            confirmationData.isNullOrBlank().not() -> inflateConfirmationFragment(confirmationData!!)
            savedInstanceState == null -> inflateBaseFragment()
        }

        (dialog as? WithBackPressedListener)?.onBackPressed = {
            requireActivity().onBackPressed()
            true
        }

        dialog?.setOnShowListener {
            dialog?.findViewById<View>(R.id.design_bottom_sheet)?.apply {
                background = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sessionReporter.resumeSession()
    }

    override fun onPause() {
        sessionReporter.pauseSession()
        super.onPause()
    }

    override fun onDestroyView() {

        (dialog as? WithBackPressedListener)?.onBackPressed = null

        super.onDestroyView()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    override fun onDismiss(dialog: DialogInterface) {
        view?.hideSoftKeyboard()

        super.onDismiss(dialog)
        activity?.finish()
    }

    private fun inflateConfirmationFragment(confirmationData: String) {
        router.screensData.observe(viewLifecycleOwner, Observer(::onConfirmationScreenChanged))
        childFragmentManager.inTransaction {
            add(
                R.id.containerBottomSheet,
                ConfirmationFragment.createFragment(confirmationData),
                CONFIRMATION_SBP_FRAGMENT_TAG
            )
        }
    }

    private fun inflateBaseFragment() {
        router.screensData.observe(viewLifecycleOwner, Observer(::onScreenChanged))
        val screenData = requireNotNull(arguments?.getParcelable(START_SCREEN_DATA_INFO)) as StartScreenData
        childFragmentManager.inTransaction {
            if (screenData.paymentMethodType.contains(PaymentMethodType.YOO_MONEY)) {
                add(
                    R.id.authContainer,
                    MoneyAuthFragment(), AUTH_FRAGMENT_TAG
                )
            }
            add(
                R.id.containerBottomSheet,
                PaymentOptionListFragment(),
                PAYMENT_OPTION_LIST_FRAGMENT_TAG
            )
        }
    }

    private fun onScreenChanged(screen: Screen) {
        val currentFragment = childFragmentManager.fragments.lastOrNull()
        when (screen) {
            is Screen.Contract -> transitToFragment(
                requireNotNull(currentFragment),
                ContractFragment(),
                CONTRACT_FRAGMENT_TAG
            )
            is Screen.PaymentOptions -> {
                with(childFragmentManager) {
                    val paymentOptionListFragment =
                        (findFragmentByTag(PAYMENT_OPTION_LIST_FRAGMENT_TAG) as PaymentOptionListFragment)

                    if (paymentOptionListFragment.isHidden) {
                        takeIf { !isStateSaved && isAdded }
                            ?.popBackStack()
                    }

                    paymentOptionListFragment.onAppear()
                }
            }
            is Screen.MoneyAuth -> {
                (childFragmentManager.findFragmentByTag(AUTH_FRAGMENT_TAG) as MoneyAuthFragment).authorize()
            }
            is Screen.Tokenize -> transitToFragment(
                requireNotNull(currentFragment),
                TokenizeFragment.newInstance(screen.tokenizeInputModel),
                TOKENIZE_FRAGMENT_TAG
            )
            is Screen.TokenizeSuccessful -> {
                val result = Intent()
                    .putExtra(EXTRA_PAYMENT_TOKEN, screen.tokenOutputModel.token)
                    .putExtra(EXTRA_PAYMENT_METHOD_TYPE, screen.tokenOutputModel.option.toType())

                with(requireActivity()) {
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            }
            is Screen.TokenizeCancelled -> with(requireActivity()) {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            is Screen.PaymentAuth -> transitToFragment(
                requireNotNull(currentFragment),
                PaymentAuthFragment.createFragment(screen.amount, screen.linkWalletToApp),
                PAYMENT_AUTH_FRAGMENT_TAG
            )
            is Screen.UnbindLinkedCard -> transitToFragment(
                requireNotNull(currentFragment),
                UnbindCardFragment.createFragment(screen.paymentOption as LinkedCard),
                UNBIND_CARD_FRAGMENT_TAG
            )
            is Screen.UnbindInstrument -> transitToFragment(
                requireNotNull(currentFragment),
                UnbindCardFragment.createFragment(screen.instrumentBankCard),
                UNBIND_CARD_FRAGMENT_TAG
            )
            else -> Unit
        }
    }

    private fun onConfirmationScreenChanged(screen: Screen) {
        val currentFragment = childFragmentManager.fragments.lastOrNull()
        when (screen) {
            is Screen.SBPConfirmation -> transitToFragment(
                requireNotNull(currentFragment),
                ConfirmationFragment.createFragment(screen.confirmationData),
                CONFIRMATION_SBP_FRAGMENT_TAG
            )
            is Screen.SBPConfirmationSuccessful -> with(requireActivity()) {
                setResult(Activity.RESULT_OK)
                finish()
            }
            is Screen.BankList -> transitToFragment(
                requireNotNull(currentFragment),
                BankListFragment.newInstance(screen.confirmationUrl, screen.paymentId),
                LIST_BANKS_FRAGMENT_TAG
            )
            else -> Unit
        }
    }

    private fun transitToFragment(fromFragment: Fragment, newFragment: Fragment, tag: String) {
        val currentFragmentRoot = fromFragment.requireView()
        childFragmentManager
            .beginTransaction()
            .apply {
                if (!isTablet) {
                    addSharedElement(currentFragmentRoot, currentFragmentRoot.transitionName)
                    setReorderingAllowed(true)
                    newFragment.sharedElementEnterTransition = BottomSheetFragmentTransition()
                }
            }
            .replace(R.id.containerBottomSheet, newFragment, tag)
            .addToBackStack(newFragment::class.java.name)
            .commit()
    }

    companion object {
        private const val START_SCREEN_DATA_INFO = "ru.yoomoney.sdk.kassa.payments.ui.START_SCREEN_DATA_INFO"

        fun createFragment(startScreenData: StartScreenData? = null): MainDialogFragment {
            return MainDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(START_SCREEN_DATA_INFO, startScreenData)
                }
            }
        }
    }
}
