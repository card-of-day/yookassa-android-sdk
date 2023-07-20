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

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.api.SBPApi
import ru.yoomoney.sdk.kassa.payments.api.jacksonBaseObjectMapper
import ru.yoomoney.sdk.kassa.payments.model.PaymentDetails
import ru.yoomoney.sdk.kassa.payments.model.mapper.toPaymentDetails
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.PrioritySbpBank
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.PrioritySbpBanksConfig
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.SbpBankInfoDomain
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.model.toSbpBankDomainList

internal interface BankListRepository {
    suspend fun getSbpBanks(confirmationUrl: String): Result<List<SbpBankInfoDomain>>

    suspend fun getPriorityBanks(context: Context): Result<List<PrioritySbpBank>>

    suspend fun getPaymentStatus(paymentId: String): Result<PaymentDetails>
}

internal class BankListRepositoryImpl(
    private val sbpApi: SBPApi,
) : BankListRepository {

    override suspend fun getSbpBanks(confirmationUrl: String): Result<List<SbpBankInfoDomain>> {
        return sbpApi.getSbpBanks(confirmationUrl).fold(
            onSuccess = { Result.success(it.banks.toSbpBankDomainList()) },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun getPriorityBanks(context: Context): Result<List<PrioritySbpBank>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val jsonString = context.resources.openRawResource(R.raw.ym_sbp_priority_banks_config)
                    .bufferedReader().use { it.readText() }
                jacksonBaseObjectMapper.readValue(jsonString, PrioritySbpBanksConfig::class.java).banks
            }
        }
    }

    override suspend fun getPaymentStatus(paymentId: String): Result<PaymentDetails> {
        return sbpApi.getPaymentDetails(paymentId).fold(
            onSuccess = { Result.success(it.toPaymentDetails()) },
            onFailure = { Result.failure(it) }
        )
    }
}
