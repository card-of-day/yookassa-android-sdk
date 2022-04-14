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

@file:JvmName("JSONArrayExtensions")

package ru.yoomoney.sdk.kassa.payments.extensions

import org.json.JSONArray
import org.json.JSONObject

internal inline fun <R> JSONArray.mapIndexed(transform: (Int, JSONObject) -> R): List<R> {
    val result = ArrayList<R>(length())
    for (i in 0 until length()) {
        result.add(transform(i, getJSONObject(i)))
    }
    return result
}

internal inline fun <R> JSONArray.mapStrings(transform: (Int, String) -> R): List<R> {
    val result = ArrayList<R>(length())
    for (i in 0 until length()) {
        result.add(transform(i, getString(i)))
    }
    return result
}