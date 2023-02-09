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

package ru.yoomoney.sdk.kassa.payments.logging

import android.content.Context
import android.util.Log
import ru.yoomoney.sdk.kassa.payments.metrics.Param
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.utils.isBuildDebug

internal class ReporterLogger(
    private val reporter: Reporter,
    private val showLogs: Boolean,
    private val context: Context
) : Reporter {
    override fun report(name: String, args: List<Param>?) {
        if (showLogs && context.isBuildDebug()) {
            if (args == null) {
                Log.d("ANALYTICS_EVENT", name)
            } else {
                Log.d("ANALYTICS_EVENT", "$name ${args.joinToString(",")}")
            }
        }
        reporter.report(name, args)
    }

    override fun report(name: String, arg: String) {
        if (showLogs && context.isBuildDebug()) {
            Log.d("ANALYTICS_EVENT", "$name - $arg")
        }
        reporter.report(name, arg)
    }

    override fun report(name: String, arg: Boolean) {
        if (showLogs && context.isBuildDebug()) {
            Log.d("ANALYTICS_EVENT", "$name - $arg")
        }
        reporter.report(name, arg)
    }
}
