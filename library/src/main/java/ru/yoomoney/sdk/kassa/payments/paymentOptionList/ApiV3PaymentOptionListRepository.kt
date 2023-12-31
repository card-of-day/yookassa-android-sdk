/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.paymentOptionList

import ru.yoomoney.sdk.kassa.payments.api.PaymentsApi
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.config.ConfigRepository
import ru.yoomoney.sdk.kassa.payments.model.CurrentUser
import ru.yoomoney.sdk.kassa.payments.model.PaymentOptionsResponse
import ru.yoomoney.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListRepository
import ru.yoomoney.sdk.kassa.payments.model.Result
import ru.yoomoney.sdk.kassa.payments.model.mapper.mapToPaymentOptionsModel

internal class ApiV3PaymentOptionListRepository(
    private val paymentsApi: PaymentsApi,
    private val configRepository: ConfigRepository,
    private val gatewayId: String?,
    private val savePaymentMethod: SavePaymentMethod,
    private val merchantCustomerId: String?
) : PaymentOptionListRepository {

    override suspend fun getPaymentOptions(amount: Amount, currentUser: CurrentUser): Result<PaymentOptionsResponse> {
        val save = when (savePaymentMethod) {
            SavePaymentMethod.ON ->  true
            SavePaymentMethod.OFF -> false
            SavePaymentMethod.USER_SELECTS -> null
        }
        return paymentsApi.getPaymentOptions(
            gatewayId = gatewayId,
            amount = amount.value.toString(),
            currency = amount.currency.toString(),
            savePaymentMethod = save,
            merchantCustomerId = merchantCustomerId
        ).fold(
            onSuccess = {
                Result.Success(it.mapToPaymentOptionsModel(configRepository.getConfig().paymentMethods))
            },
            onFailure = {
                Result.Fail(it)
            }
        )
    }
}
