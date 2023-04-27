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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Keep
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

private const val KEY_LOAD_URL = "loadUrl"
private const val KEY_REDIRECT_URL = "returnUrl"

@Keep
internal class WebViewFragment : Fragment() {

    private val listener: Listener by lazy {
        requireActivity() as Listener
    }
    private var webView: WebView? = null

    private val loadUrl: String by lazy {
        arguments?.getString(KEY_LOAD_URL).orEmpty()
    }
    private val redirectUrl: String? by lazy {
        arguments?.getString(KEY_REDIRECT_URL)
    }

    init {
        retainInstance = true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = checkNotNull(context) { "Context should be present here" }
        val appContext = context.applicationContext

        webView = WebView(appContext, null, 0).apply {
            isFocusableInTouchMode = true
            settings.javaScriptEnabled = true
            @Suppress("DEPRECATION")
            settings.saveFormData = false
            webViewClient = WebViewClientImpl(redirectUrl, listener)
            webChromeClient = WebChromeClient()
        }
        load(loadUrl)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView?.canGoBack() == true) {
                        webView?.goBack()
                    } else {
                        listener.onCloseActivity()
                        requireActivity().finish()
                    }
                }
            }
        )
        return webView
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.also { (it.parent as ViewGroup?)?.removeView(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
        webView = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString(KEY_LOAD_URL, loadUrl)
            putString(KEY_REDIRECT_URL, redirectUrl)
        }
    }

    fun load(url: String) {
        val webView = checkNotNull(webView) { "load should be called after fragment initialization" }
        webView.loadUrl(url)
    }

    interface Listener {
        fun onShowProgress()
        fun onHideProgress()
        fun onError(errorCode: Int, description: String?, failingUrl: String?)
        fun onSuccess()
        fun onCloseActivity()
    }

    companion object {
        fun create(loadUrl: String, redirectUrl: String) = WebViewFragment().apply {
            arguments = bundleOf(KEY_LOAD_URL to loadUrl, KEY_REDIRECT_URL to redirectUrl)
        }
    }
}
