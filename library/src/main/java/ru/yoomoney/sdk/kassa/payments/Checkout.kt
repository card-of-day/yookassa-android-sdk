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

package ru.yoomoney.sdk.kassa.payments

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutActivity
import ru.yoomoney.sdk.kassa.payments.ui.EXTRA_CREATED_WITH_CHECKOUT_METHOD
import ru.yoomoney.sdk.kassa.payments.ui.EXTRA_CSC_PARAMETERS
import ru.yoomoney.sdk.kassa.payments.ui.EXTRA_PAYMENT_PARAMETERS
import ru.yoomoney.sdk.kassa.payments.ui.EXTRA_PAYMENT_TOKEN
import ru.yoomoney.sdk.kassa.payments.ui.EXTRA_TEST_PARAMETERS
import ru.yoomoney.sdk.kassa.payments.ui.EXTRA_UI_PARAMETERS
import ru.yoomoney.sdk.kassa.payments.ui.color.InMemoryColorSchemeRepository
import ru.yoomoney.sdk.kassa.payments.utils.checkUrl
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavedBankCardPaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.confirmation.ConfirmationActivity
import ru.yoomoney.sdk.kassa.payments.confirmation.EXTRA_CLIENT_APPLICATION_KEY
import ru.yoomoney.sdk.kassa.payments.confirmation.EXTRA_CONFIRMATION_URL
import ru.yoomoney.sdk.kassa.payments.confirmation.EXTRA_PAYMENT_METHOD_TYPE
import ru.yoomoney.sdk.kassa.payments.ui.color.ColorScheme
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_CARD_NUMBER
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_EXPIRY_MONTH
import ru.yoomoney.sdk.kassa.payments.ui.view.EXTRA_EXPIRY_YEAR
import ru.yoomoney.sdk.kassa.payments.utils.WebViewActivity

/**
 * Entry point for MSDK. All actions related to MSDK should be performed through this class.
 */
object Checkout {

    @Keep
    const val EXTRA_ERROR_CODE = "ru.yoomoney.sdk.kassa.payments.extra.ERROR_CODE"
    @Keep
    const val EXTRA_ERROR_DESCRIPTION = "ru.yoomoney.sdk.kassa.payments.extra.ERROR_DESCRIPTION"
    @Keep
    const val EXTRA_ERROR_FAILING_URL = "ru.yoomoney.sdk.kassa.payments.extra.ERROR_FAILING_URL"
    @Keep
    const val RESULT_ERROR = Activity.RESULT_FIRST_USER
    @Keep
    const val ERROR_NOT_HTTPS_URL = Int.MIN_VALUE

    /**
     * Create [Intent] to start tokenization process.
     * This intent should be used in startActivityForResult() to start tokenization.
     *
     * When tokenization is finished, result will return in onActivityResult().
     * ResultCode can be Activity.RESULT_OK or Activity.RESULT_CANCELED.
     * To retrieve successful result use [createTokenizationResult].
     *
     * @param context application context.
     * @param paymentParameters parameters of your shop, see [PaymentParameters].
     * @param testParameters (optional) debug parameters, see [TestParameters].
     * @param uiParameters (optional) visual settings, see [UiParameters].
     */
    @[JvmStatic JvmOverloads Keep]
    fun createTokenizeIntent(
        context: Context,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters = TestParameters(),
        uiParameters: UiParameters = UiParameters()
    ): Intent {
        return Intent(context, CheckoutActivity::class.java)
            .putExtra(EXTRA_UI_PARAMETERS, uiParameters)
            .putExtra(EXTRA_PAYMENT_PARAMETERS, paymentParameters)
            .putExtra(EXTRA_TEST_PARAMETERS, testParameters)
            .putExtra(EXTRA_CREATED_WITH_CHECKOUT_METHOD, true)
    }

    /**
     * Create tokenization result from data in onActivityResult().
     *
     * @param data intent, received after tokenization (see [createTokenizeIntent]).
     * Intents, received from other sources will cause typecast exception.
     */
    @[JvmStatic Keep]
    fun createTokenizationResult(data: Intent): TokenizationResult {
        val token = requireNotNull(data.getStringExtra(EXTRA_PAYMENT_TOKEN))
        val type = data.getSerializableExtra(EXTRA_PAYMENT_METHOD_TYPE) as PaymentMethodType
        return TokenizationResult(token, type)
    }

    /**
     * Create [Intent] to start 3DS [Activity].
     * You should not use this method if [PaymentParameters.customReturnUrl] is present.
     * @param context [Context] to create [Intent].
     * @param url Url to open 3DS, should be valid https url.
     * @return [Intent] to start 3DS [Activity].
     *
     * @deprecated use [createConfirmationIntent] instead.
     */
    @Deprecated(
        message = "Use method createConfirmationIntent() instead",
        replaceWith = ReplaceWith(
            expression = "Checkout.createConfirmationIntent(context=context, confirmationUrl=url, paymentMethodType=)",
            imports = ["ru.yoomoney.sdk.kassa.payments.Checkout"]
        )
    )
    @[JvmStatic JvmOverloads Keep]
    fun create3dsIntent(
        context: Context,
        url: String,
        colorScheme: ColorScheme = ColorScheme.getDefaultScheme(),
        testParameters: TestParameters = TestParameters()
    ): Intent {
        checkUrl(url)
        InMemoryColorSchemeRepository.colorScheme = colorScheme
        return WebViewActivity.create(
            context = context,
            url = url,
            logParam = "openScreen3ds",
            testParameters = testParameters
        )
    }

    /**
     * Create scan result intent for bank card scanning.
     * @param cardNumber card number, may be separated with spaces, should be no more than 23 symbols length.
     * @param expirationMonth card expiration month, should be between 1 and 12.
     * @param expirationYear card expiration year, should be last 2 digits of year.
     * @return [Intent] with bank card data.
     */
    @[JvmStatic Keep]
    fun createScanBankCardIntent(cardNumber: String, expirationMonth: Int, expirationYear: Int): Intent {
        require(cardNumber.length <= 23) { "cardNumber should be no more than 23 symbols length" }
        require(expirationMonth in 1..12) { "expirationMonth should be between 1 and 12" }
        require(expirationYear in 0..99) { "expirationYear should be last 2 digits of year" }

        return Intent()
            .putExtra(EXTRA_CARD_NUMBER, cardNumber)
            .putExtra(EXTRA_EXPIRY_MONTH, expirationMonth)
            .putExtra(EXTRA_EXPIRY_YEAR, expirationYear)
    }

    /**
     * Create [Intent] to start tokenization process with a paymentId of user's bank card.
     * This method should be used if you have saved user's card and want user to re-enter csc.
     * In other cases you should use [createTokenizeIntent].
     *
     * This intent should be used in startActivityForResult() to start tokenization.
     *
     * When tokenization is finished, result will return in onActivityResult().
     * ResultCode can be Activity.RESULT_OK or Activity.RESULT_CANCELED.
     * To retrieve successful result use [createTokenizationResult].
     *
     * @param context application context.
     * @param savedBankCardPaymentParameters parameters of your shop and saved card, see [SavedBankCardPaymentParameters].
     * @param testParameters (optional) debug parameters, see [TestParameters].
     * @param uiParameters (optional) visual settings, see [UiParameters].
     */
    @[JvmStatic JvmOverloads Keep]
    fun createSavedCardTokenizeIntent(
        context: Context,
        savedBankCardPaymentParameters: SavedBankCardPaymentParameters,
        testParameters: TestParameters = TestParameters(),
        uiParameters: UiParameters = UiParameters()
    ): Intent {
        return Intent(context, CheckoutActivity::class.java)
            .putExtra(EXTRA_UI_PARAMETERS, uiParameters)
            .putExtra(EXTRA_CSC_PARAMETERS, savedBankCardPaymentParameters)
            .putExtra(EXTRA_TEST_PARAMETERS, testParameters)
            .putExtra(EXTRA_CREATED_WITH_CHECKOUT_METHOD, true)
    }

    /**
     * Create [Intent] to start payment process via app confirmation
     *
     * This intent should be used in startActivityForResult() to start payment process via app confirmation
     *
     * When payment is finished, result will return in onActivityResult().
     * ResultCode can be Activity.RESULT_OK in case if payment in successfully done
     * or Activity.RESULT_CANCELED if user cancel the payment.
     *
     * @param context application context.
     * @param confirmationUrl an url you retrieved from API to start payment process
     * @param colorScheme color of the scheme selected in the settings
     * @param paymentMethodType selected type of payment
     */
    @[JvmStatic JvmOverloads Keep]
    fun createConfirmationIntent(
        context: Context,
        confirmationUrl: String,
        paymentMethodType: PaymentMethodType,
        clientApplicationKey: String? = null,
        colorScheme: ColorScheme = ColorScheme.getDefaultScheme(),
        testParameters: TestParameters = TestParameters()
    ): Intent = when(paymentMethodType) {
        PaymentMethodType.SBERBANK, PaymentMethodType.SBP -> {
            Intent(context, ConfirmationActivity::class.java)
                .putExtra(EXTRA_CONFIRMATION_URL, confirmationUrl)
                .putExtra(EXTRA_PAYMENT_METHOD_TYPE, paymentMethodType)
                .putExtra(EXTRA_TEST_PARAMETERS, testParameters)
                .putExtra(EXTRA_CLIENT_APPLICATION_KEY, clientApplicationKey)
        }
        else -> {
            checkUrl(confirmationUrl)
            InMemoryColorSchemeRepository.colorScheme = colorScheme
            WebViewActivity.create(
                context = context,
                url = confirmationUrl,
                logParam = "openScreen3ds",
                testParameters = testParameters
            )
        }
    }
}
