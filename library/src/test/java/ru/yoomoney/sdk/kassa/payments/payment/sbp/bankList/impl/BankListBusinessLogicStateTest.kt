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

import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Action
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.State
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpBankInfoDomain
import ru.yoomoney.sdk.march.generateBusinessLogicTests

@RunWith(Parameterized::class)
internal class BankListBusinessLogicStateTest(
    @Suppress("unused")
    val testName: String,
    val state: State,
    val action: Action,
    val expected: State,
) {

    companion object {
        private val confirmationUrl = "www.yoomoney.ru/confirm"
        private val paymentId = "12345"
        private val throwable = Throwable()
        private val bankDeeplink: String = "yoomoney.ru/pay"
        private val shortBankList = listOf(
            SbpBankInfoDomain("memberId1", "name1", "nameEng2", "deeplink1"),
            SbpBankInfoDomain("memberId2", "name2", "nameEng2", "deeplink2"),
            SbpBankInfoDomain("memberId3", "name3", "nameEng3", "deeplink3"),
        )

        private val fullBankList = listOf(
            SbpBankInfoDomain("memberId1", "name1", "nameEng2", "deeplink1"),
            SbpBankInfoDomain("memberId2", "name2", "nameEng2", "deeplink2"),
            SbpBankInfoDomain("memberId3", "name3", "nameEng3", "deeplink3"),
            SbpBankInfoDomain("memberId4", "name4", "nameEng4", "deeplink4"),
            SbpBankInfoDomain("memberId5", "name5", "nameEng5", "deeplink5"),
        )

        @[Parameterized.Parameters(name = "{0}") JvmStatic]
        fun data(): Collection<Array<out Any>> {
            val errorState = State.Error(throwable)
            val fullBankListContentState = State.FullBankListContent(fullBankList, true)
            val shortBankListContentState = State.ShortBankListContent(shortBankList, fullBankList)
            val progressState = State.Progress
            val fullBankListStatusProgress = State.FullBankListStatusProgress(fullBankList, true)
            val shortBankListStatusProgress = State.ShortBankListStatusProgress(shortBankList, fullBankList)
            val paymentFullBankListStatusError = State.PaymentFullBankListStatusError(throwable, fullBankList, true)
            val paymentShortBankListStatusError = State.PaymentShortBankListStatusError(throwable, shortBankList, fullBankList)
            val loadBankListAction = Action.LoadBankList
            val loadBankListFailedAction = Action.LoadBankListFailed(throwable)
            val loadShortBankListSuccessAction = Action.LoadShortBankListSuccess(shortBankList, fullBankList)
            val loadFullBankListSuccessAction = Action.LoadFullBankListSuccess(fullBankList, shortBankList, true)
            val backToBankListAction = Action.BackToBankList
            val loadPaymentStatusAction = Action.LoadPaymentStatus
            val paymentStatusErrorAction = Action.PaymentStatusError(throwable)
            val bankInteractionFinished = Action.BankInteractionFinished
            val paymentProcessFinished = Action.PaymentProcessFinished
            val selectBankAction = Action.SelectBank(bankDeeplink)
            val loadOtherBankListAction = Action.LoadOtherBankList
            val paymentProcessInProgress = Action.PaymentProcessInProgress
            val activityNotFoundState = State.ActivityNotFoundError(throwable, shortBankListContentState)
            val activityNotFoundStateFullBankList = State.ActivityNotFoundError(throwable, fullBankListContentState)
            val activityNotFoundAction = Action.ActivityNotFound(throwable)
            val searchAction = Action.Search("sber")
            val cancelSearchAction = Action.CancelSearch
            return generateBusinessLogicTests<State, Action>(
                generateState = { kClassState ->
                    when (kClassState) {
                        State.Error::class -> errorState
                        State.FullBankListContent::class -> fullBankListContentState
                        State.ShortBankListContent::class -> shortBankListContentState
                        State.Progress::class -> progressState
                        State.FullBankListStatusProgress::class -> fullBankListStatusProgress
                        State.ShortBankListStatusProgress::class -> shortBankListStatusProgress
                        State.PaymentFullBankListStatusError::class -> paymentFullBankListStatusError
                        State.PaymentShortBankListStatusError::class -> paymentShortBankListStatusError
                        State.ActivityNotFoundError::class -> activityNotFoundState
                        else -> kClassState.objectInstance ?: error(kClassState)
                    }
                },
                generateAction = { kClassAction ->
                    when (kClassAction) {
                        Action.LoadBankList::class -> loadBankListAction
                        Action.LoadBankListFailed::class -> loadBankListFailedAction
                        Action.LoadShortBankListSuccess::class -> loadShortBankListSuccessAction
                        Action.LoadFullBankListSuccess::class -> loadFullBankListSuccessAction
                        Action.BackToBankList::class -> backToBankListAction
                        Action.LoadPaymentStatus::class -> loadPaymentStatusAction
                        Action.PaymentStatusError::class -> paymentStatusErrorAction
                        Action.BankInteractionFinished::class -> bankInteractionFinished
                        Action.PaymentProcessFinished::class -> paymentProcessFinished
                        Action.SelectBank::class -> selectBankAction
                        Action.LoadOtherBankList::class -> loadOtherBankListAction
                        Action.PaymentProcessInProgress::class -> paymentProcessInProgress
                        Action.ActivityNotFound::class -> activityNotFoundAction
                        Action.Search::class -> searchAction
                        Action.CancelSearch::class -> cancelSearchAction
                        else -> kClassAction.objectInstance ?: error(kClassAction)
                    }
                },
                generateExpectation = { state, action ->
                    when (state to action) {
                        progressState to loadFullBankListSuccessAction -> fullBankListContentState
                        progressState to loadShortBankListSuccessAction -> shortBankListContentState
                        progressState to loadBankListFailedAction -> errorState
                        shortBankListContentState to selectBankAction -> shortBankListContentState
                        fullBankListContentState to selectBankAction -> fullBankListContentState
                        shortBankListContentState to bankInteractionFinished -> shortBankListStatusProgress
                        fullBankListContentState to bankInteractionFinished -> fullBankListStatusProgress
                        shortBankListStatusProgress to paymentStatusErrorAction -> paymentShortBankListStatusError
                        fullBankListStatusProgress to paymentStatusErrorAction -> paymentFullBankListStatusError
                        paymentShortBankListStatusError to loadPaymentStatusAction -> shortBankListStatusProgress
                        paymentFullBankListStatusError to loadPaymentStatusAction -> fullBankListStatusProgress
                        fullBankListStatusProgress to paymentProcessInProgress -> fullBankListContentState
                        shortBankListStatusProgress to paymentProcessInProgress -> shortBankListContentState
                        fullBankListStatusProgress to paymentProcessFinished -> fullBankListStatusProgress
                        shortBankListStatusProgress to paymentProcessFinished -> shortBankListStatusProgress
                        shortBankListContentState to backToBankListAction -> shortBankListContentState
                        fullBankListContentState to loadShortBankListSuccessAction -> shortBankListContentState
                        shortBankListContentState to loadOtherBankListAction -> fullBankListContentState
                        errorState to loadBankListAction -> progressState
                        shortBankListContentState to activityNotFoundAction -> activityNotFoundState
                        activityNotFoundState to backToBankListAction -> shortBankListContentState
                        fullBankListContentState to activityNotFoundAction -> activityNotFoundStateFullBankList
                        paymentShortBankListStatusError to bankInteractionFinished -> shortBankListStatusProgress
                        paymentFullBankListStatusError to bankInteractionFinished -> fullBankListStatusProgress
                        fullBankListContentState to searchAction -> fullBankListContentState.copy(searchText = searchAction.searchText)
                        fullBankListContentState to cancelSearchAction -> fullBankListContentState
                        else -> state
                    }
                }
            )
        }
    }

    private val logic = BankListBusinessLogic(
        showState = mock(),
        showEffect = mock(),
        source = mock(),
        interactor = mock(),
        confirmationUrl = confirmationUrl,
        paymentId = paymentId,
    )

    @Test
    fun test() {
        // when
        val actual = logic(state, action)

        // then
        Assert.assertEquals(expected, actual.state)
    }
}