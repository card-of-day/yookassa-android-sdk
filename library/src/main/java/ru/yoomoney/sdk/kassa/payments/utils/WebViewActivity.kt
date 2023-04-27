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

package ru.yoomoney.sdk.kassa.payments.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.yandex.metrica.YandexMetrica
import ru.yoomoney.sdk.kassa.payments.BuildConfig
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.Checkout.EXTRA_ERROR_CODE
import ru.yoomoney.sdk.kassa.payments.Checkout.EXTRA_ERROR_DESCRIPTION
import ru.yoomoney.sdk.kassa.payments.Checkout.EXTRA_ERROR_FAILING_URL
import ru.yoomoney.sdk.kassa.payments.Checkout.RESULT_ERROR
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.extensions.visible
import ru.yoomoney.sdk.kassa.payments.logging.ReporterLogger
import ru.yoomoney.sdk.kassa.payments.metrics.YandexMetricaReporter

private const val ACTION_CLOSE_3DS_SCREEN = "close3dsScreen"

const val EXTRA_URL = "ru.yoomoney.sdk.kassa.payments.extra.URL"
const val EXTRA_TEST_PARAMETERS = "ru.yoomoney.sdk.kassa.payments.extra.TEST_PARAMETERS"
const val EXTRA_LOG_PARAM = "ru.yoomoney.sdk.kassa.payments.extra.LOG_PARAM"

class WebViewActivity : AppCompatActivity(), WebViewFragment.Listener {

    private lateinit var reporterLogger: ReporterLogger

    private var progress: ContentLoadingProgressBar? = null
    private var showProgress: Boolean = false
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reporterLogger = ReporterLogger(
            YandexMetricaReporter(YandexMetrica.getReporter(applicationContext, BuildConfig.APP_METRICA_KEY)),
            (requireNotNull(intent.getParcelableExtra(EXTRA_TEST_PARAMETERS)) as TestParameters).showLogs,
            this
        )

        val url = requireNotNull(intent.getStringExtra(EXTRA_URL))
        val logParam = intent.getStringExtra(EXTRA_LOG_PARAM)

        if (checkUrl(url)) {
            if (savedInstanceState == null && logParam != null) {
                reporterLogger.report(logParam)
            }

            setContentView(R.layout.ym_activity_web_view)
            title = null
            if (savedInstanceState == null) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, WebViewFragment.create(url, DEFAULT_REDIRECT_URL))
                    .commit()
            }
        } else {
            onError(Checkout.ERROR_NOT_HTTPS_URL, "Not https:// url", url)
        }
    }

    override fun setTitle(title: CharSequence?) {
        toolbar?.also {
            val parent = it.parent
            if (parent is CollapsingToolbarLayout) {
                parent.title = title
            } else {
                super.setTitle(title)
            }
        } ?: super.setTitle(title)
    }

    override fun onContentChanged() {
        toolbar = findViewById<Toolbar>(R.id.toolbar)?.also { toolbar ->
            toolbar.setNavigationOnClickListener {
                reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, false)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            toolbar.changeToolbarButtonColor()
        }
        toolbar?.inflateMenu(R.menu.ym_menu_activity_webview)
        val actionView = toolbar?.menu?.findItem(R.id.progress)?.actionView
        progress = ((actionView as ViewGroup).getChildAt(0) as ContentLoadingProgressBar?)?.apply {
            val size = resources.getDimensionPixelSize(R.dimen.ym_checkout_web_view_activity_progress_size)
            with(layoutParams) {
                width = size
                height = size
            }

            val padding = resources.getDimensionPixelSize(R.dimen.ym_space_m)
            setPadding(padding, padding, padding, padding)

            when (showProgress) {
                true -> onShowProgress()
                false -> onHideProgress()
            }
        }
    }

    override fun onCloseActivity() {
        reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, false)
    }

    override fun onShowProgress() {
        showProgress = true

        if (progress?.visible == false) {
            progress?.show()
        }
    }

    override fun onHideProgress() {
        showProgress = false

        if (progress?.visible == true) {
            progress?.hide()
        }
    }

    override fun onError(errorCode: Int, description: String?, failingUrl: String?) {
        reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, false)
        setResult(
            RESULT_ERROR,
            Intent()
                .putExtras(intent)
                .putExtra(EXTRA_ERROR_CODE, errorCode)
                .putExtra(EXTRA_ERROR_DESCRIPTION, description)
                .putExtra(EXTRA_ERROR_FAILING_URL, failingUrl)
        )
        finish()
    }

    override fun onSuccess() {
        reporterLogger.report(ACTION_CLOSE_3DS_SCREEN, true)
        setResult(RESULT_OK, Intent().putExtras(intent))
        finish()
    }

    companion object {
        fun create(
            context: Context,
            url: String,
            logParam: String? = null,
            testParameters: TestParameters
        ): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TEST_PARAMETERS, testParameters)
                if (logParam != null) {
                    putExtra(EXTRA_LOG_PARAM, logParam)
                }
            }
        }

        fun checkUrl(url: String) = URLUtil.isHttpsUrl(url) || BuildConfig.DEBUG && URLUtil.isAssetUrl(url)
    }
}
