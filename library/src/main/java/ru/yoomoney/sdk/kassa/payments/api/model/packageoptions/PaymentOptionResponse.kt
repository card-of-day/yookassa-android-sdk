/*
 * The MIT License (MIT)
 * Copyright © 2022 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.api.model.packageoptions

import com.fasterxml.jackson.annotation.JsonProperty

internal sealed class PaymentOptionResponse

internal data class PaymentOptionBankCard(
    @JsonProperty("payment_method_type")
    val paymentMethodType: PaymentMethodType,

    @JsonProperty("confirmation_types")
    val confirmationTypes: List<ConfirmationType>?,

    @JsonProperty("charge")
    val charge: Amount,

    @JsonProperty("fee")
    val fee: Fee?,

    @JsonProperty("save_payment_method")
    val savePaymentMethod: SavePaymentMethod,

    @JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean,

    @JsonProperty("identification_requirement")
    val identificationRequirement: IdentificationRequirement?,

    @JsonProperty("payment_instruments")
    val paymentInstruments: List<PaymentInstrumentBankCard>?

) : PaymentOptionResponse()

internal sealed class PaymentInstrumentYooMoney : PaymentOptionResponse() {

    data class PaymentInstrumentYooMoneyWallet(

        @JsonProperty("payment_method_type")
        val paymentMethodType: PaymentMethodType,

        @JsonProperty("confirmation_types")
        val confirmationTypes: List<ConfirmationType>?,

        @JsonProperty("charge")
        val charge: Amount,

        @JsonProperty("fee")
        val fee: Fee?,

        @JsonProperty("save_payment_method")
        val savePaymentMethod: SavePaymentMethod,

        @JsonProperty("save_payment_instrument")
        val savePaymentInstrument: Boolean,

        @JsonProperty("identification_requirement")
        val identificationRequirement: IdentificationRequirement?,

        @JsonProperty("instrument_type")
        val instrumentType: InstrumentType,

        @JsonProperty("id")
        val id: String?,

        @JsonProperty("balance")
        val balance: Amount?,
    ) : PaymentInstrumentYooMoney()

    data class PaymentInstrumentYooMoneyLinkedBankCard(

        @JsonProperty("payment_method_type")
        val paymentMethodType: PaymentMethodType,

        @JsonProperty("confirmation_types")
        val confirmationTypes: List<ConfirmationType>?,

        @JsonProperty("charge")
        val charge: Amount,

        @JsonProperty("fee")
        val fee: Fee?,

        @JsonProperty("save_payment_method")
        val savePaymentMethod: SavePaymentMethod,

        @JsonProperty("save_payment_instrument")
        val savePaymentInstrument: Boolean,

        @JsonProperty("identification_requirement")
        val identificationRequirement: IdentificationRequirement?,

        @JsonProperty("instrument_type")
        val instrumentType: InstrumentType,

        @JsonProperty("id")
        val id: String,

        @JsonProperty("card_name")
        val cardName: String?,

        @JsonProperty("card_mask")
        val cardMask: String,

        @JsonProperty("card_type")
        val cardType: BankCardType

    ): PaymentInstrumentYooMoney()

    data class AbstractYooMoneyWallet(

        @JsonProperty("payment_method_type")
        val paymentMethodType: PaymentMethodType,

        @JsonProperty("charge")
        val charge: Amount,

        @JsonProperty("fee")
        val fee: Fee?,

        @JsonProperty("save_payment_method")
        val savePaymentMethod: SavePaymentMethod,

        @JsonProperty("confirmation_types")
        val confirmationTypes: List<ConfirmationType>?,

        @JsonProperty("save_payment_instrument")
        val savePaymentInstrument: Boolean
    ): PaymentInstrumentYooMoney()
}

internal data class PaymentOptionSberbank(
    @JsonProperty("payment_method_type")
    val paymentMethodType: PaymentMethodType,

    @JsonProperty("confirmation_types")
    val confirmationTypes: List<ConfirmationType>?,

    @JsonProperty("charge")
    val charge: Amount,

    @JsonProperty("fee")
    val fee: Fee?,

    @JsonProperty("save_payment_method")
    val savePaymentMethod: SavePaymentMethod,

    @JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean,

    @JsonProperty("identification_requirement")
    val identificationRequirement: IdentificationRequirement?,
) : PaymentOptionResponse()

internal data class PaymentOptionGooglePay(
    @JsonProperty("payment_method_type")
    val paymentMethodType: PaymentMethodType,

    @JsonProperty("confirmation_types")
    val confirmationTypes: List<ConfirmationType>?,

    @JsonProperty("charge")
    val charge: Amount,

    @JsonProperty("fee")
    val fee: Fee?,

    @JsonProperty("save_payment_method")
    val savePaymentMethod: SavePaymentMethod,

    @JsonProperty("save_payment_instrument")
    val savePaymentInstrument: Boolean,

    @JsonProperty("identification_requirement")
    val identificationRequirement: IdentificationRequirement?,

    @JsonProperty("payment_systems_allowed")
    val paymentSystemsAllowed: BankCardType,
) : PaymentOptionResponse()

internal object Unknown : PaymentOptionResponse()