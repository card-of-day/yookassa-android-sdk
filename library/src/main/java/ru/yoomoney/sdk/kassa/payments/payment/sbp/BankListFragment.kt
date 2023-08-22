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

package ru.yoomoney.sdk.kassa.payments.payment.sbp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ViewAnimator
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.errorFormatter.ErrorFormatter
import ru.yoomoney.sdk.kassa.payments.extensions.hideSoftKeyboard
import ru.yoomoney.sdk.kassa.payments.extensions.showChild
import ru.yoomoney.sdk.kassa.payments.extensions.visible
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListInteractor
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListViewModel
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListViewModelFactory
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.BankListViewModelFactory.Companion.BANK_LIST_FEATURE
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutTextInputView
import ru.yoomoney.sdk.kassa.payments.ui.DialogTopBar
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.ui.changeHeightWithMobileAnimation
import ru.yoomoney.sdk.kassa.payments.ui.changeViewWithMobileAnimation
import ru.yoomoney.sdk.kassa.payments.ui.getViewHeight
import ru.yoomoney.sdk.kassa.payments.ui.view.ErrorView
import ru.yoomoney.sdk.kassa.payments.ui.view.LoadingView
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.observe
import javax.inject.Inject

private const val CONFIRMATION_URL_KEY = "CONFIRMATION_URL_KEY"
private const val PAYMENT_ID_KEY = "PAYMENT_ID_KEY"

internal class BankListFragment : Fragment(R.layout.ym_bank_list_fragment) {

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var viewModelFactory: BankListViewModelFactory.AssistedBankListVmFactory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var bankListInteractor: BankListInteractor

    private val confirmationUrl: String by lazy { arguments?.getString(CONFIRMATION_URL_KEY).orEmpty() }
    private val paymentId: String by lazy { arguments?.getString(PAYMENT_ID_KEY).orEmpty() }

    private val viewModel: BankListViewModel by viewModel(BANK_LIST_FEATURE) {
        viewModelFactory.create(
            BankListViewModelFactory.BankListAssistedParams(
                confirmationUrl,
                paymentId
            )
        )
    }

    private lateinit var topBar: DialogTopBar
    private lateinit var banksRecyclerView: RecyclerView
    private lateinit var bankListAdapter: BankListAdapter
    private lateinit var rootContainer: ViewAnimator
    private lateinit var loadingView: LoadingView
    private lateinit var errorView: ErrorView
    private lateinit var contentLinear: LinearLayout
    private lateinit var searchInputView: CheckoutTextInputView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.injectBankListFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar = view.findViewById(R.id.topBar) as DialogTopBar
        banksRecyclerView = view.findViewById(R.id.banksRecyclerView) as RecyclerView
        rootContainer = view.findViewById(R.id.rootContainer) as ViewAnimator

        loadingView = view.findViewById(R.id.loadingView) as LoadingView
        errorView = view.findViewById(R.id.errorView) as ErrorView
        contentLinear = view.findViewById(R.id.contentLinear)
        searchInputView = view.findViewById(R.id.searchInputView)

        topBar.title = getString(R.string.ym_sbp_select_bank_title)
        bankListAdapter = BankListAdapter(
            onClickBank = { viewModel.handleAction(BankList.Action.SelectBank(it)) },
            onClickSelectAnotherBank = { viewModel.handleAction(BankList.Action.LoadOtherBankList) }
        )
        banksRecyclerView.adapter = bankListAdapter
        banksRecyclerView.itemAnimator = null
        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = ::showEffect,
            onFail = {}
        )
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    searchInputView.text = ""
                    rootContainer.hideSoftKeyboard()
                    viewModel.handleAction(BankList.Action.BackToBankList)
                }
            }
        )
        searchInputView.editText.doAfterTextChanged { text: Editable? ->
            viewModel.handleAction(BankList.Action.Search(text?.toString().orEmpty()))
        }
        searchInputView.setImeOptions(EditorInfo.IME_ACTION_DONE)
    }

    override fun onResume() {
        super.onResume()
        if (bankListInteractor.bankWasSelected) {
            viewModel.handleAction(BankList.Action.BankInteractionFinished)
        }
    }

    private fun showEffect(effect: BankList.Effect) {
        when (effect) {
            is BankList.Effect.OpenBank -> openBank(effect.deeplink)
            is BankList.Effect.CloseBankList -> {
                (parentFragment as? MainDialogFragment)?.dismiss()
            }

            is BankList.Effect.CloseBankListWithFinish -> {
                router.navigateTo(Screen.SBPConfirmationSuccessful)
            }
        }
    }

    private fun showState(state: BankList.State) {
        val showSearch = state is BankList.State.FullBankListContent
        searchInputView.visible = showSearch
        when (state) {
            is BankList.State.Progress,
            is BankList.State.ShortBankListStatusProgress,
            is BankList.State.FullBankListStatusProgress,
            -> showProgress()

            is BankList.State.ShortBankListContent -> showShortBankListContent(state)
            is BankList.State.FullBankListContent -> showFullBankListContent(state)
            is BankList.State.Error -> showError(state.throwable) {
                viewModel.handleAction(BankList.Action.LoadBankList)
            }

            is BankList.State.PaymentShortBankListStatusError -> showError(state.throwable) {
                viewModel.handleAction(BankList.Action.LoadPaymentStatus)
            }

            is BankList.State.PaymentFullBankListStatusError -> showError(state.throwable) {
                viewModel.handleAction(BankList.Action.LoadPaymentStatus)
            }

            is BankList.State.ActivityNotFoundError -> showError(
                state.throwable,
                buttonText = getString(R.string.ym_understand_button)
            ) {
                viewModel.handleAction(BankList.Action.BackToBankList)
            }
        }
    }

    private fun openBank(deeplink: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(deeplink)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            viewModel.handleAction(BankList.Action.ActivityNotFound(exception))
        }
    }

    private fun showError(
        throwable: Throwable,
        buttonText: String = getString(R.string.ym_retry),
        onClickRetry: () -> Unit,
    ) {
        errorView.setErrorText(errorFormatter.format(throwable))
        errorView.setErrorButtonListener { onClickRetry.invoke() }
        errorView.setErrorButtonText(buttonText)
        changeViewWithMobileAnimation(rootContainer) {
            rootContainer.showChild(errorView)
        }
    }

    private fun showShortBankListContent(shortBankListContent: BankList.State.ShortBankListContent) {
        topBar.onBackButton(null, true)
        contentLinear.minimumHeight = 0
        val rootContainterHeightWas = rootContainer.getViewHeight()
        bankListAdapter.submitList(
            mapToShortBankListViewEntities(requireContext(), shortBankListContent.shortBankList)
        ) {
            rootContainer.showChild(contentLinear)
            changeHeightWithMobileAnimation(
                rootContainer,
                rootContainterHeightWas,
                rootContainer.getViewHeight()
            )
        }
    }

    private fun showFullBankListContent(fullBankListContent: BankList.State.FullBankListContent) {
        if (fullBankListContent.showBackNavigation) {
            topBar.onBackButton(
                {
                    searchInputView.text = ""
                    rootContainer.hideSoftKeyboard()
                    viewModel.handleAction(BankList.Action.BackToBankList)
                },
                true
            )
        } else {
            topBar.onBackButton(null, true)
        }
        val rootContainterHeightWas = rootContainer.getViewHeight()
        val isSearchingNow = fullBankListContent.searchText.isEmpty().not()
        val bankList = if (isSearchingNow) {
            contentLinear.minimumHeight = rootContainer.getViewHeight()
            fullBankListContent.searchedBanks
        } else {
            contentLinear.minimumHeight = 0
            fullBankListContent.bankList
        }
        bankListAdapter.submitList(mapToFullBankListViewEntities(bankList)) {
            if (contentLinear.visible.not()) {
                rootContainer.showChild(contentLinear)
            }
            changeHeightWithMobileAnimation(
                rootContainer,
                rootContainterHeightWas,
                rootContainer.getViewHeight()
            )
        }
    }

    private fun showProgress() {
        changeViewWithMobileAnimation(rootContainer) {
            rootContainer.showChild(loadingView)
        }
    }

    companion object {
        fun newInstance(confirmationUrl: String, paymentId: String): BankListFragment {
            return BankListFragment().apply {
                arguments = bundleOf(
                    CONFIRMATION_URL_KEY to confirmationUrl,
                    PAYMENT_ID_KEY to paymentId
                )
            }
        }
    }
}
