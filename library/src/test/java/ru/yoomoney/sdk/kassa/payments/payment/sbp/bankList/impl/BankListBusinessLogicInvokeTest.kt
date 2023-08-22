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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Action
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.Effect
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList.State
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpBankInfoDomain
import ru.yoomoney.sdk.kassa.payments.utils.func


internal class BankListBusinessLogicInvokeTest {
    private val showState: (State) -> Action = mock()
    private val showEffect: (Effect) -> Unit = mock()
    private val source: () -> Action = mock()
    private val confirmationUrl = "www.yoomoney.ru/confirm"
    private val paymentId = "12345"
    private val bankListInteractor: BankListInteractor = mock()
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

    private val logic =
        BankListBusinessLogic(showState, showEffect, source, bankListInteractor, confirmationUrl, paymentId)


    @Test
    fun `should show ShortBankListContent state with loading and LoadShortBankListSuccess action`() {
        runBlocking {
            val expectedState = State.ShortBankListContent(shortBankList, fullBankList)
            val out = logic(State.Progress, Action.LoadShortBankListSuccess(shortBankList, fullBankList))

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show FullBankListContent state with loading and LoadFullBankListSuccess action`() {
        runBlocking {
            val expectedState = State.FullBankListContent(shortBankList, true)
            val out = logic(State.Progress, Action.LoadFullBankListSuccess(shortBankList, fullBankList, true))

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show Error state with loading and getSbpBanks`() {
        runBlocking {

            val expectedState = State.Error(throwable)
            whenever(bankListInteractor.getSbpBanks(confirmationUrl)).doReturn(Action.LoadBankListFailed(throwable))
            val out = logic(State.Progress, bankListInteractor.getSbpBanks(confirmationUrl))

            out.sources.func()

            verify(showState).invoke(expectedState)
            verify(bankListInteractor).getSbpBanks(confirmationUrl)
        }
    }

    @Test
    fun `should show Progress state with Error state and LoadBankList action`() {
        runBlocking {
            val expectedState = State.Progress
            whenever(bankListInteractor.getSbpBanks(confirmationUrl)).doReturn(Action.LoadShortBankListSuccess(shortBankList, fullBankList))
            val out = logic(State.Error(throwable), Action.LoadBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
            verify(bankListInteractor).getSbpBanks(confirmationUrl)
        }
    }

    @Test
    fun `should show FullBankListContent state with ShortBankListContent state and LoadOtherBankList action`() {
        runBlocking {
            val expectedState = State.FullBankListContent(fullBankList, true)
            val out = logic(State.ShortBankListContent(shortBankList, fullBankList), Action.LoadOtherBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show CloseBankList effect and BackToShortBankList action`() {
        runBlocking {
            val out = logic(State.ShortBankListContent(shortBankList, fullBankList), Action.BackToBankList)

            out.sources.func()

            verify(showEffect).invoke(Effect.CloseBankList)
            verify(source).invoke()
        }
    }

    @Test
    fun `should show OpenBank effect and SelectBank action`() {
        runBlocking {
            val out = logic(State.ShortBankListContent(shortBankList, fullBankList), Action.SelectBank(bankDeeplink))

            out.sources.func()

            verify(showEffect).invoke(Effect.OpenBank(bankDeeplink))
            verify(source).invoke()
        }
    }

    @Test
    fun `should show ShortBankListStatusProgress state with ShortBankListContent state and BankInteractionFinished action`() {
        runBlocking {
            val expectedState = State.ShortBankListStatusProgress(shortBankList, fullBankList)
            val out = logic(State.ShortBankListContent(shortBankList, fullBankList), Action.BankInteractionFinished)

            out.sources.func()

            verify(showState).invoke(expectedState)
            verify(bankListInteractor).getPaymentStatus(paymentId)
        }
    }

    @Test
    fun `should show CloseBankList effect and FullBankListContent state and  BackToShortBankList action`() {
        runBlocking {
            val out = logic(State.FullBankListContent(shortBankList, false), Action.BackToBankList)

            out.sources.func()

            verify(showEffect).invoke(Effect.CloseBankList)
            verify(source).invoke()
        }
    }

    @Test
    fun `should invoke bankListInteractor with FullBankListContent state and  BackToShortBankList action`() {
        runBlocking {
            whenever(bankListInteractor.getPriorityBanks(fullBankList)).doReturn(Action.LoadShortBankListSuccess(shortBankList, fullBankList))
            val out = logic(State.FullBankListContent(fullBankList, true), Action.BackToBankList)

            out.sources.func()

            verify(bankListInteractor).getPriorityBanks(fullBankList)
        }
    }

    @Test
    fun `should show ShortBankListContent state with FullBankListContent state and  BackToShortBankList action`() {
        runBlocking {
            val expectedState = State.ShortBankListContent(shortBankList, fullBankList)
            whenever(bankListInteractor.getPriorityBanks(fullBankList)).doReturn(Action.LoadShortBankListSuccess(shortBankList, fullBankList))
            val out = logic(State.FullBankListContent(fullBankList, true), bankListInteractor.getPriorityBanks(fullBankList))

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show OpenBank effect with FullBankListContent state and  SelectBank action`() {
        runBlocking {
            val out = logic(State.FullBankListContent(fullBankList, true), Action.SelectBank(bankDeeplink))

            out.sources.func()

            verify(showEffect).invoke(Effect.OpenBank(bankDeeplink))
            verify(source).invoke()
        }
    }

    @Test
    fun `should show FullBankListStatusProgress state with FullBankListContent state and  BankInteractionFinished action`() {
        runBlocking {
            val expectedState = State.FullBankListStatusProgress(fullBankList, true)
            whenever(bankListInteractor.getPaymentStatus(paymentId)).doReturn(Action.PaymentProcessFinished)
            val out = logic(State.FullBankListContent(fullBankList, true), Action.BankInteractionFinished)

            out.sources.func()

            verify(showState).invoke(expectedState)
            verify(bankListInteractor).getPaymentStatus(paymentId)
        }
    }

    @Test
    fun `should show ShortBankListContent state with ShortBankListStatusProgress state and  PaymentProcessInProgress action`() {
        runBlocking {
            val expectedState = State.ShortBankListContent(shortBankList, fullBankList)
            val out = logic(State.ShortBankListStatusProgress(shortBankList, fullBankList), Action.PaymentProcessInProgress)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show CloseBankListWithFinish effect with ShortBankListStatusProgress state and  PaymentProcessFinished action`() {
        runBlocking {
            val out = logic(State.ShortBankListStatusProgress(shortBankList, fullBankList), Action.PaymentProcessFinished)

            out.sources.func()

            verify(showEffect).invoke(Effect.CloseBankListWithFinish)
            verify(source).invoke()
        }
    }

    @Test
    fun `should show FullBankListContent state with ShortBankListStatusProgress state and  PaymentProcessInProgress action`() {
        runBlocking {
            val expectedState = State.FullBankListContent(fullBankList, true)
            val out = logic(State.FullBankListStatusProgress(fullBankList, true), Action.PaymentProcessInProgress)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show CloseBankListWithFinish effect with FullBankListStatusProgress state and  PaymentProcessFinished action`() {
        runBlocking {
            val out = logic(State.FullBankListStatusProgress(fullBankList, true), Action.PaymentProcessFinished)

            out.sources.func()

            verify(showEffect).invoke(Effect.CloseBankListWithFinish)
            verify(source).invoke()
        }
    }

    @Test
    fun `should show Progress state with Error state and  LoadBankList action`() {
        runBlocking {
            val expectedState = State.Progress
            val out = logic(State.Error(throwable), Action.LoadBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show FullBankListStatusProgress state with PaymentFullBankListStatusError state and  LoadPaymentStatus action`() {
        runBlocking {
            val expectedState = State.FullBankListStatusProgress(fullBankList, true)
            val out = logic(State.PaymentFullBankListStatusError(throwable, fullBankList, true), Action.LoadPaymentStatus)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show FullBankListStatusProgress state with PaymentFullBankListStatusError state and  BankInteractionFinished action`() {
        runBlocking {
            val expectedState = State.FullBankListStatusProgress(fullBankList, true)
            val out = logic(State.PaymentFullBankListStatusError(throwable, fullBankList, true), Action.BankInteractionFinished)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show ShortBankListStatusProgress state with PaymentShortBankListStatusError state and  LoadPaymentStatus action`() {
        runBlocking {
            val expectedState = State.ShortBankListStatusProgress(shortBankList, fullBankList)
            val out = logic(State.PaymentShortBankListStatusError(throwable, shortBankList, fullBankList), Action.LoadPaymentStatus)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show ShortBankListStatusProgress state with PaymentShortBankListStatusError state and  BankInteractionFinished action`() {
        runBlocking {
            val expectedState = State.ShortBankListStatusProgress(shortBankList, fullBankList)
            val out = logic(State.PaymentShortBankListStatusError(throwable, shortBankList, fullBankList), Action.BankInteractionFinished)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show ActivityNotFoundError state with PaymentFullBankList state and  ActivityNotFound action`() {
        runBlocking {
            val fullBankListContent = State.FullBankListContent(fullBankList, true)
            val expectedState = State.ActivityNotFoundError(throwable, fullBankListContent)
            val out = logic(fullBankListContent, Action.ActivityNotFound(throwable))

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show ActivityNotFoundError state with PaymentShortBankList state and  ActivityNotFound action`() {
        runBlocking {
            val shortBankListContent = State.ShortBankListContent(shortBankList, fullBankList)
            val expectedState = State.ActivityNotFoundError(throwable, shortBankListContent)
            val out = logic(shortBankListContent, Action.ActivityNotFound(throwable))

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show ShortBankListState state with ActivityNotFoundState state and BackToBankList action`() {
        runBlocking {
            val expectedState = State.ShortBankListContent(shortBankList, fullBankList)
            val activityNotFoundError = State.ActivityNotFoundError(throwable, expectedState)
            val out = logic(activityNotFoundError, Action.BackToBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }

    @Test
    fun `should show FullBankListState state with ActivityNotFoundState state and BackToBankList action`() {
        runBlocking {
            val expectedState = State.FullBankListContent(fullBankList, true)
            val activityNotFoundError = State.ActivityNotFoundError(throwable, expectedState)
            val out = logic(activityNotFoundError, Action.BackToBankList)

            out.sources.func()

            verify(showState).invoke(expectedState)
        }
    }
}