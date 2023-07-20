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

package ru.yoomoney.sdk.kassa.payments.payment.sbp

import androidx.recyclerview.widget.DiffUtil

internal class BankListDiffCallback :
    DiffUtil.ItemCallback<BankListViewEntity>() {

    override fun areItemsTheSame(
        oldItem: BankListViewEntity,
        newItem: BankListViewEntity
    ): Boolean {
        if (oldItem::class != newItem::class) return false
        return when (oldItem) {
            is BankListViewEntity.BankViewEntity -> {
                newItem as BankListViewEntity.BankViewEntity
                oldItem.title == newItem.title && oldItem.deeplink == newItem.deeplink && oldItem.memberId == newItem.memberId
            }
            is BankListViewEntity.SelectAnotherBankViewEntity -> {
                newItem as BankListViewEntity.SelectAnotherBankViewEntity
                oldItem.title == newItem.title
            }
            else -> true
        }
    }

    override fun areContentsTheSame(
        oldItem: BankListViewEntity,
        newItem: BankListViewEntity
    ): Boolean {
        return oldItem == newItem
    }
}
