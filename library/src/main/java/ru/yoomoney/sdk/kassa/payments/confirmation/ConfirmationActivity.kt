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

package ru.yoomoney.sdk.kassa.payments.confirmation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.metrics.SberPayConfirmationStatusSuccess
import ru.yoomoney.sdk.kassa.payments.payment.sbp.bankList.impl.SBP_CONFIRMATION_ACTION
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.ui.TAG_BOTTOM_SHEET
import ru.yoomoney.sdk.kassa.payments.ui.model.StartScreenData
import ru.yoomoney.sdk.kassa.payments.utils.EXTRA_TEST_PARAMETERS
import ru.yoomoney.sdk.kassa.payments.utils.INVOICING_AUTHORITY
import ru.yoomoney.sdk.kassa.payments.utils.SBERPAY_PATH
import ru.yoomoney.sdk.kassa.payments.utils.SBP_PATH
import ru.yoomoney.sdk.kassa.payments.utils.createSberbankIntent
import ru.yoomoney.sdk.kassa.payments.utils.getSberbankPackage
import javax.inject.Inject

internal const val EXTRA_PAYMENT_METHOD_TYPE = "ru.yoomoney.sdk.kassa.payments.extra.PAYMENT_METHOD_TYPE"
internal const val EXTRA_CONFIRMATION_URL = "ru.yoomoney.sdk.kassa.payments.extra.EXTRA_CONFIRMATION_URL"
internal const val EXTRA_CLIENT_APPLICATION_KEY = "ru.yoomoney.sdk.kassa.payments.extra.EXTRA_CLIENT_APPLICATION_KEY"

private const val SBER_PAY_CONFIRMATION_ACTION = "actionSberPayConfirmation"

internal class ConfirmationActivity : AppCompatActivity() {

    private var isWaitingSberForResult = false
    private val paymentMethodType: PaymentMethodType? by lazy {
        intent.getSerializableExtra(EXTRA_PAYMENT_METHOD_TYPE) as PaymentMethodType?
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @Inject
    lateinit var reporter: Reporter

    override fun onCreate(savedInstanceState: Bundle?) {
        val confirmationUrl = intent.getStringExtra(EXTRA_CONFIRMATION_URL)
        val clientApplicationKey = intent.getStringExtra(EXTRA_CLIENT_APPLICATION_KEY)
        val testParameters = intent.getParcelableExtra(EXTRA_TEST_PARAMETERS) as TestParameters? ?: TestParameters()

        CheckoutInjector.setupComponent(
            isConfirmation = true,
            context = this,
            clientApplicationKey = clientApplicationKey,
            testParameters = testParameters,
        )

        CheckoutInjector.injectConfirmationActivity(this)
        if (confirmationUrl != null) {
            super.onCreate(savedInstanceState)
            startConfirmationProcess(
                confirmationUrl,
                paymentMethodType,
                testParameters
            )
            return
        } else {
            handleIntent(intent)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (paymentMethodType == PaymentMethodType.SBERBANK) {
            if (isWaitingSberForResult) {
                isWaitingSberForResult = false
                setResult(Activity.RESULT_CANCELED)
                finish()
            } else {
                isWaitingSberForResult = true
            }
        }
    }

    private fun startConfirmationProcess(
        confirmationUrl: String,
        paymentMethodType: PaymentMethodType?,
        testParameters: TestParameters,
    ) {
        when (paymentMethodType) {
            PaymentMethodType.SBERBANK -> {
                startActivity(
                    createSberbankIntent(
                        this,
                        confirmationUrl,
                        getSberbankPackage(testParameters.hostParameters.isDevHost)
                    )
                )
            }

            PaymentMethodType.SBP -> {
                showDialog(supportFragmentManager)
            }

            else -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onDestroy() {
        detachMainDialogFragment()
        super.onDestroy()
    }

    private fun detachMainDialogFragment() {
        findDialog(supportFragmentManager)?.dialog?.apply {
            setOnCancelListener(null)
            setOnDismissListener(null)
        }
    }

    private fun showDialog(supportFragmentManager: FragmentManager) {
        val startScreenData = StartScreenData(
            confirmationData = intent.getStringExtra(EXTRA_CONFIRMATION_URL)
        )
        findDialog(supportFragmentManager) ?: MainDialogFragment.createFragment(startScreenData)
            .show(supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    private fun findDialog(supportFragmentManager: FragmentManager) =
        supportFragmentManager.findFragmentByTag(TAG_BOTTOM_SHEET) as MainDialogFragment?

    private fun handleIntent(newIntent: Intent) {
        val sberPayResult =
            newIntent.data?.authority == INVOICING_AUTHORITY && newIntent.data?.path?.contains(SBERPAY_PATH) == true
        val sbpResult = newIntent.data?.authority == SBP_PATH
        when {
            sberPayResult -> {
                setResult(Activity.RESULT_OK)
                isWaitingSberForResult = false
                reporter.report(SBER_PAY_CONFIRMATION_ACTION, listOf(SberPayConfirmationStatusSuccess()))
            }

            sbpResult -> {
                setResult(Activity.RESULT_OK)
                reporter.report(SBP_CONFIRMATION_ACTION)
            }

            else -> setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }
}
