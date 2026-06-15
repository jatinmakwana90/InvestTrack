package com.investtrack.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.investtrack.R
import com.investtrack.data.models.Folio
import com.investtrack.databinding.ItemFolioBinding
import com.investtrack.databinding.ItemTransactionBinding
import com.investtrack.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class FolioAdapter(
    private val onClick: (Folio) -> Unit
) : ListAdapter<Folio, FolioAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemFolioBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(f: Folio) {
            b.tvSecurityName.text = f.securityName
            b.tvSymbol.text = f.securitySymbol.ifEmpty { f.securityType.displayName }
            b.tvSecurityType.text = f.securityType.displayName
            b.tvCurrentValue.text = CurrencyFormatter.format(f.currentValue)
            b.tvUnits.text = "Units: ${String.format("%.3f", f.totalUnits)}"
            b.tvAvgCost.text = "Avg: ${CurrencyFormatter.format(f.avgCostPrice)}"
            b.tvCmp.text = "CMP: ${CurrencyFormatter.format(f.currentPrice)}"
            b.tvPnL.text = "${if (f.pnl >= 0) "+" else ""}${CurrencyFormatter.format(f.pnl)} (${String.format("%.2f", f.pnlPercent)}%)"
            b.tvPnL.setTextColor(b.root.context.getColor(if (f.pnl >= 0) R.color.gain_green else R.color.loss_red))
            if (f.folioNumber.isNotEmpty()) {
                b.tvFolioNo.text = "Folio: ${f.folioNumber}"
                b.tvFolioNo.visibility = android.view.View.VISIBLE
            } else {
                b.tvFolioNo.visibility = android.view.View.GONE
            }
            b.root.setOnClickListener { onClick(f) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemFolioBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Folio>() {
            override fun areItemsTheSame(a: Folio, b: Folio) = a.accountId == b.accountId && a.securityId == b.securityId
            override fun areContentsTheSame(a: Folio, b: Folio) = a == b
        }
    }
}

class TransactionAdapter : ListAdapter<TransactionItem, TransactionAdapter.VH>(DIFF) {

    private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class VH(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: TransactionItem) {
            val txn = item.transaction
            val security = item.security
            b.tvSecurityName.text = security?.name ?: "Unknown"
            b.tvTxnType.text = txn.type.displayName
            b.tvTxnType.setBackgroundResource(
                when {
                    txn.type.name.contains("BUY") || txn.type == com.investtrack.data.models.TransactionType.SIP -> R.drawable.bg_type_buy
                    txn.type.name.contains("SELL") || txn.type == com.investtrack.data.models.TransactionType.SWP -> R.drawable.bg_type_sell
                    else -> R.drawable.bg_type_other
                }
            )
            b.tvAmount.text = CurrencyFormatter.format(txn.amount)
            b.tvQtyPrice.text = "Qty: ${String.format("%.3f", txn.quantity)} @ ${CurrencyFormatter.format(txn.price)}"
            b.tvDate.text = dateFmt.format(Date(txn.transactionDate))
            if (txn.folioNumber.isNotEmpty()) b.tvFolio.text = "Folio: ${txn.folioNumber}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TransactionItem>() {
            override fun areItemsTheSame(a: TransactionItem, b: TransactionItem) = a.transaction.id == b.transaction.id
            override fun areContentsTheSame(a: TransactionItem, b: TransactionItem) = a == b
        }
    }
}
