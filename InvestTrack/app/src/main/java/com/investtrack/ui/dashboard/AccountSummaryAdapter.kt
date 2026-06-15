package com.investtrack.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.investtrack.R
import com.investtrack.databinding.ItemAccountSummaryBinding
import com.investtrack.utils.CurrencyFormatter

class AccountSummaryAdapter(
    private val onClick: (AccountSummaryItem) -> Unit
) : ListAdapter<AccountSummaryItem, AccountSummaryAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemAccountSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AccountSummaryItem) {
            binding.tvAccountName.text = item.account.name
            binding.tvAccountType.text = item.account.type.name.replace("_", " ")
            binding.tvCurrentValue.text = CurrencyFormatter.format(item.summary.currentValue)
            binding.tvInvested.text = "Inv: ${CurrencyFormatter.format(item.summary.totalInvested)}"
            val pnl = item.summary.totalPnL
            val pct = item.summary.totalPnLPercent
            binding.tvPnL.text = "${if (pnl >= 0) "▲" else "▼"} ${CurrencyFormatter.format(kotlin.math.abs(pnl))} (${String.format("%.2f", kotlin.math.abs(pct))}%)"
            binding.tvPnL.setTextColor(
                binding.root.context.getColor(if (pnl >= 0) R.color.gain_green else R.color.loss_red)
            )
            binding.tvHoldings.text = "${item.summary.folioCount} holdings"
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemAccountSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AccountSummaryItem>() {
            override fun areItemsTheSame(a: AccountSummaryItem, b: AccountSummaryItem) = a.account.id == b.account.id
            override fun areContentsTheSame(a: AccountSummaryItem, b: AccountSummaryItem) = a == b
        }
    }
}
