package com.investtrack.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.investtrack.R
import com.investtrack.databinding.FragmentAccountDetailBinding
import com.investtrack.utils.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountDetailFragment : Fragment() {

    private var _binding: FragmentAccountDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountDetailViewModel by viewModels()
    private lateinit var folioAdapter: FolioAdapter
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupTabs()
        observeState()

        binding.fabAddTransaction.setOnClickListener {
            val action = AccountDetailFragmentDirections.actionAccountDetailToAddTransaction(viewModel.accountId)
            findNavController().navigate(action)
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupAdapters() {
        folioAdapter = FolioAdapter { folio ->
            val action = AccountDetailFragmentDirections.actionAccountDetailToFolioDetail(
                folio.accountId, folio.securityId
            )
            findNavController().navigate(action)
        }
        transactionAdapter = TransactionAdapter()

        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        viewModel.setViewMode(ViewMode.FOLIO)
                        binding.rvContent.adapter = folioAdapter
                    }
                    1 -> {
                        viewModel.setViewMode(ViewMode.TRANSACTION)
                        binding.rvContent.adapter = transactionAdapter
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding.rvContent.adapter = folioAdapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.account?.let { binding.toolbar.title = it.name }
                state.summary?.let { summary ->
                    binding.tvCurrentValue.text = CurrencyFormatter.format(summary.currentValue)
                    binding.tvInvested.text = "Invested: ${CurrencyFormatter.format(summary.totalInvested)}"
                    val pnl = summary.totalPnL
                    binding.tvPnL.text = "${if (pnl >= 0) "+" else ""}${CurrencyFormatter.format(pnl)} (${String.format("%.2f", summary.totalPnLPercent)}%)"
                    binding.tvPnL.setTextColor(
                        requireContext().getColor(if (pnl >= 0) R.color.gain_green else R.color.loss_red)
                    )
                }
                folioAdapter.submitList(state.folios)
                transactionAdapter.submitList(state.transactions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
