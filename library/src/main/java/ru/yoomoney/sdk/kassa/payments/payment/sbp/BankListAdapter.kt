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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.yoomoney.sdk.gui.widgetV2.list.item_action.ItemVectorPrimaryActionView
import ru.yoomoney.sdk.gui.widgetV2.list.item_detail.ItemVectorPrimaryDetailView
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.ui.view.ErrorView

internal const val BANK_ITEM_VIEW_TYPE = 1
internal const val SELECT_ANOTHER_BANK_VIEW_TYPE = 2
internal const val DIVIDER_VIEW_TYPE = 3
internal const val EMPTY_STATE_TYPE = 4

internal sealed class BankListViewEntity {

    data class BankViewEntity(val memberId: String, val title: String, val deeplink: String) : BankListViewEntity()

    data class SelectAnotherBankViewEntity(val title: String) : BankListViewEntity()

    object Divider : BankListViewEntity()

    object EmptyState : BankListViewEntity()
}

internal class BankListAdapter(val onClickBank: (String) -> Unit, val onClickSelectAnotherBank: () -> Unit) :
    ListAdapter<BankListViewEntity, BankListViewHolder>(BankListDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is BankListViewEntity.BankViewEntity -> BANK_ITEM_VIEW_TYPE
            is BankListViewEntity.SelectAnotherBankViewEntity -> SELECT_ANOTHER_BANK_VIEW_TYPE
            is BankListViewEntity.Divider -> DIVIDER_VIEW_TYPE
            is BankListViewEntity.EmptyState -> EMPTY_STATE_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankListViewHolder {
        return when (viewType) {
            BANK_ITEM_VIEW_TYPE -> BankListViewHolder.BankViewHolder(parent.context, onClickBank)
            SELECT_ANOTHER_BANK_VIEW_TYPE -> BankListViewHolder.SelectAnotherBankViewHolder(
                parent.context,
                onClickSelectAnotherBank
            )

            DIVIDER_VIEW_TYPE -> BankListViewHolder.DividerViewHolder(parent)
            EMPTY_STATE_TYPE -> BankListViewHolder.EmptyStateViewHolder(parent)
            else -> throw IllegalStateException("viewType: $viewType is not supported")
        }
    }

    override fun onBindViewHolder(holder: BankListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

internal sealed class BankListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bind(item: BankListViewEntity)

    class BankViewHolder(context: Context, val onClickBank: (String) -> Unit) :
        BankListViewHolder(ItemVectorPrimaryDetailView(context)) {

        override fun bind(item: BankListViewEntity) {
            val bankItem = item as BankListViewEntity.BankViewEntity
            (view as ItemVectorPrimaryDetailView).apply {
                title = bankItem.title
                setOnClickListener { onClickBank(bankItem.deeplink) }
            }
            view.updateLayoutParams {
                height = view.context.resources.getDimensionPixelSize(R.dimen.ym_item_min_height_normal)
            }
        }
    }

    class SelectAnotherBankViewHolder(context: Context, val onClickSelectAnotherBank: () -> Unit) :
        BankListViewHolder(ItemVectorPrimaryActionView(context)) {

        override fun bind(item: BankListViewEntity) {
            val selectAnotherBankItem = item as BankListViewEntity.SelectAnotherBankViewEntity
            (view as ItemVectorPrimaryActionView).apply {
                title = selectAnotherBankItem.title
                setOnClickListener { onClickSelectAnotherBank() }
            }
            view.updateLayoutParams {
                height = view.context.resources.getDimensionPixelSize(R.dimen.ym_item_min_height_normal)
            }
        }
    }

    class DividerViewHolder(parent: ViewGroup) :
        BankListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.ym_divider, parent, false)) {

        override fun bind(item: BankListViewEntity) {
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = view.context.resources.getDimensionPixelSize(R.dimen.ym_spaceM)
            }
        }
    }

    class EmptyStateViewHolder(parent: ViewGroup) :
        BankListViewHolder(ErrorView(parent.context)) {

        override fun bind(item: BankListViewEntity) {
            (view as ErrorView).apply {
                setErrorTitle(context.getString(R.string.ym_title_not_found))
                setErrorText(context.getString(R.string.ym_subtitle_sbp_not_found))
                setIcon(requireNotNull(ContextCompat.getDrawable(context, R.drawable.ym_search_not_found)))
            }
            view.layoutParams = LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            (view.layoutParams as MarginLayoutParams).apply {
                this.setMargins(0, view.context.resources.getDimensionPixelSize(R.dimen.ym_space_3xl), 0, 0)
            }
        }
    }
}
