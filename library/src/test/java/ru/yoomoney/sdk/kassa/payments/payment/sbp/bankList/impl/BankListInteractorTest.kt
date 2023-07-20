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

import android.content.pm.PackageInfo
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.BankList
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpBankInfoDomain

@RunWith(RobolectricTestRunner::class)
internal class BankListInteractorTest {

    private val fullBankList = listOf(
        SbpBankInfoDomain("100000000111", "Sber", "Sber", "deeplink1"),
        SbpBankInfoDomain("100000000004", "Tinkoff", "Tinkoff", "deeplink2"),
        SbpBankInfoDomain("memberId3", "name3", "nameEng3", "deeplink3"),
        SbpBankInfoDomain("memberId4", "name4", "nameEng4", "deeplink4"),
        SbpBankInfoDomain("memberId5", "name5", "nameEng5", "deeplink5"),
    )

    val bankListInteractor = BankListInteractorImpl(RuntimeEnvironment.getApplication().applicationContext,
        BankListRepositoryImpl(mock()))

    @Test
    fun `should return shortBankList with installed apps`() {
        runBlocking {
            val shadowPackageManager = Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager)
            shadowPackageManager.installPackage(PackageInfo().apply { packageName = "ru.sberbankmobile" })
            shadowPackageManager.installPackage(PackageInfo().apply { packageName = "com.idamob.tinkoff.android" })

            val banksAction = bankListInteractor.getPriorityBanks(fullBankList)

            assert(banksAction is BankList.Action.LoadShortBankListSuccess && banksAction.shortBankList.size == 2 && banksAction.fullBankList == fullBankList)
        }
    }

    @Test
    fun `should return empty priority banks, user has no priority installed apps`() {
        runBlocking {
            val banksAction = bankListInteractor.getPriorityBanks(fullBankList)

            assert(banksAction is BankList.Action.LoadShortBankListSuccess && banksAction.shortBankList.isEmpty() && banksAction.fullBankList == fullBankList)
        }
    }
}