package com.investtrack.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun format(amount: Double): String {
        return when {
            amount >= 10_000_000 -> "₹${String.format("%.2f", amount / 10_000_000)}Cr"
            amount >= 100_000 -> "₹${String.format("%.2f", amount / 100_000)}L"
            else -> "₹${String.format("%,.2f", amount)}"
        }
    }

    fun formatFull(amount: Double): String = inrFormat.format(amount)
}
