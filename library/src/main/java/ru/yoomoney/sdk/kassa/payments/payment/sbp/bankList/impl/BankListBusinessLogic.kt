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

package ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl

import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.march.Logic
import ru.yoomoney.sdk.march.Out
import ru.yoomoney.sdk.march.input
import ru.yoomoney.sdk.march.output

internal class BankListBusinessLogic(
    val showState: suspend (BankList.State) -> BankList.Action,
    val showEffect: suspend (BankList.Effect) -> Unit,
    val source: suspend () -> BankList.Action,
    val interactor: BankListInteractor,
    val confirmationUrl: String,
    val paymentId: String,
) : Logic<BankList.State, BankList.Action> {

    override fun invoke(
        state: BankList.State,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> {
        return when (state) {
            is BankList.State.Progress -> handleProgress(state, action)
            is BankList.State.Error -> handleError(state, action)
            is BankList.State.ShortBankListContent -> handleShortBankListContent(state, action)
            is BankList.State.FullBankListContent -> handleFullBankListContent(state, action)
            is BankList.State.ShortBankListStatusProgress -> handleShortBankListStatusProgress(state, action)
            is BankList.State.FullBankListStatusProgress -> handleFullBankListStatusProgress(state, action)
            is BankList.State.PaymentShortBankListStatusError -> handleShortBankListPaymentStatusError(state, action)
            is BankList.State.PaymentFullBankListStatusError -> handleFullBankListPaymentStatusError(state, action)
            is BankList.State.ActivityNotFoundError -> handleActivityNotFoundStatusError(state, action)
        }
    }

    private fun handleShortBankListContent(
        state: BankList.State.ShortBankListContent,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadOtherBankList -> Out(BankList.State.FullBankListContent(state.fullBankList, true)) {
            input { showState(this.state) }
        }

        is BankList.Action.BackToBankList -> Out(state) {
            input(source)
            output { showEffect(BankList.Effect.CloseBankList) }
        }

        is BankList.Action.SelectBank -> handleOpenBank(state, action.deeplink)
        is BankList.Action.BankInteractionFinished -> handleBankInteractionFinishedShortList(state)
        is BankList.Action.ActivityNotFound -> handleActivityNotFound(action.throwable, state)
        else -> Out.skip(state, source)
    }

    private fun handleBankInteractionFinishedShortList(state: BankList.State.ShortBankListContent) =
        Out(
            BankList.State.ShortBankListStatusProgress(
                state.shortBankList,
                state.fullBankList
            )
        ) {
            input { showState(this.state) }
            input { interactor.getPaymentStatus(paymentId) }
        }

    private fun handleFullBankListContent(
        fullBankListContent: BankList.State.FullBankListContent,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.BackToBankList -> Out(fullBankListContent) {
            input(source)
            if (fullBankListContent.showBackNavigation) {
                input { interactor.getPriorityBanks(fullBankListContent.bankList) }
            } else {
                output { showEffect(BankList.Effect.CloseBankList) }
            }
        }

        is BankList.Action.LoadShortBankListSuccess -> Out(
            BankList.State.ShortBankListContent(
                action.shortBankList,
                fullBankListContent.bankList
            )
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.SelectBank -> handleOpenBank(fullBankListContent, action.deeplink)
        is BankList.Action.BankInteractionFinished -> handleBankInteractionFinishedFullList(fullBankListContent)

        is BankList.Action.ActivityNotFound -> handleActivityNotFound(action.throwable, fullBankListContent)
        is BankList.Action.Search -> Out(
            fullBankListContent.copy(
                searchText = action.searchText,
                searchedBanks = interactor.searchBank(action.searchText, fullBankListContent.bankList)
            )
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.CancelSearch -> Out(
            fullBankListContent.copy(
                searchText = "",
                searchedBanks = emptyList()
            )
        ) {
            input { showState(this.state) }
        }

        else -> Out.skip(fullBankListContent, source)
    }

    private fun handleBankInteractionFinishedFullList(fullBankListContent: BankList.State.FullBankListContent) =
        Out(
            BankList.State.FullBankListStatusProgress(
                fullBankListContent.bankList,
                fullBankListContent.showBackNavigation
            )
        ) {
            input { showState(this.state) }
            input { interactor.getPaymentStatus(paymentId) }
        }

    private fun handleProgress(
        state: BankList.State.Progress,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadShortBankListSuccess -> Out(
            BankList.State.ShortBankListContent(
                action.shortBankList,
                action.fullBankList
            )
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.LoadFullBankListSuccess -> Out(
            BankList.State.FullBankListContent(
                action.fullBankList,
                action.showBackNavigation
            )
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.LoadBankListFailed -> Out(BankList.State.Error(action.throwable)) {
            input { showState(this.state) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleShortBankListStatusProgress(
        state: BankList.State.ShortBankListStatusProgress,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.PaymentProcessInProgress -> Out(
            BankList.State.ShortBankListContent(
                state.shortBankList,
                state.fullBankList
            )
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.PaymentProcessFinished -> Out(state) {
            input(source)
            output { showEffect(BankList.Effect.CloseBankListWithFinish) }
        }

        is BankList.Action.PaymentStatusError -> Out(
            BankList.State.PaymentShortBankListStatusError(
                action.throwable,
                state.shortBankList,
                state.fullBankList
            )
        ) {
            input { showState(this.state) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleFullBankListStatusProgress(
        state: BankList.State.FullBankListStatusProgress,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.PaymentProcessInProgress -> Out(
            BankList.State.FullBankListContent(
                state.fullBankList,
                state.showBackNavigation
            )
        ) {
            input { showState(this.state) }
        }

        is BankList.Action.PaymentProcessFinished -> Out(state) {
            input(source)
            output { showEffect(BankList.Effect.CloseBankListWithFinish) }
        }

        is BankList.Action.PaymentStatusError -> Out(
            BankList.State.PaymentFullBankListStatusError(
                action.throwable,
                state.fullBankList,
                state.showBackNavigation
            )
        ) {
            input { showState(this.state) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleError(
        state: BankList.State.Error,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadBankList -> Out(BankList.State.Progress) {
            input { showState(this.state) }
            input { interactor.getSbpBanks(confirmationUrl) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleShortBankListPaymentStatusError(
        state: BankList.State.PaymentShortBankListStatusError,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadPaymentStatus, BankList.Action.BankInteractionFinished -> Out(
            BankList.State.ShortBankListStatusProgress(
                state.shortBankList,
                state.fullBankList
            )
        ) {
            input { showState(this.state) }
            input { interactor.getPaymentStatus(paymentId) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleFullBankListPaymentStatusError(
        state: BankList.State.PaymentFullBankListStatusError,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.LoadPaymentStatus, BankList.Action.BankInteractionFinished -> Out(
            BankList.State.FullBankListStatusProgress(
                state.bankList,
                state.showBackNavigation
            )
        ) {
            input { showState(this.state) }
            input { interactor.getSbpBanks(confirmationUrl) }
        }

        else -> Out.skip(state, source)
    }

    private fun handleOpenBank(state: BankList.State, deeplink: String) = Out(state) {
        input(source)
        output { showEffect(BankList.Effect.OpenBank(deeplink)) }
        interactor.bankWasSelected = true
    }

    private fun handleActivityNotFound(throwable: Throwable, state: BankList.State) =
        Out(BankList.State.ActivityNotFoundError(throwable, state)) {
            input { showState(this.state) }
        }

    private fun handleActivityNotFoundStatusError(
        state: BankList.State.ActivityNotFoundError,
        action: BankList.Action,
    ): Out<BankList.State, BankList.Action> = when (action) {
        is BankList.Action.BackToBankList -> Out(state.previosListState) {
            input { showState(this.state) }
        }

        is BankList.Action.BankInteractionFinished -> Out(state) {
            input { showState(this.state) }
        }

        else -> Out.skip(state, source)
    }
}
