package com.investtrack.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.investtrack.R
import com.investtrack.databinding.FragmentDashboardBinding
import com.investtrack.utils.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var accountAdapter: AccountSummaryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        observeState()
        binding.fabAddAccount.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addAccount)
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadDashboard() }
        binding.btnViewAllAccounts.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_accounts)
        }
    }

    private fun setupRecyclerViews() {
        accountAdapter = AccountSummaryAdapter { accountItem ->
            val action = DashboardFragmentDirections.actionDashboardToAccountDetail(accountItem.account.id)
            findNavController().navigate(action)
        }
        binding.rvAccounts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = accountAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.swipeRefresh.isRefreshing = state.isLoading

                state.portfolioSummary?.let { summary ->
                    binding.tvTotalValue.text = CurrencyFormatter.format(summary.currentValue)
                    binding.tvTotalInvested.text = "Invested: ${CurrencyFormatter.format(summary.totalInvested)}"
                    val pnlText = "${if (summary.totalPnL >= 0) "+" else ""}${CurrencyFormatter.format(summary.totalPnL)} (${String.format("%.2f", summary.totalPnLPercent)}%)"
                    binding.tvTotalPnL.text = pnlText
                    binding.tvTotalPnL.setTextColor(
                        if (summary.totalPnL >= 0) requireContext().getColor(R.color.gain_green)
                        else requireContext().getColor(R.color.loss_red)
                    )
                    binding.tvAccountCount.text = "${summary.accountCount} Accounts"
                    binding.tvFolioCount.text = "${summary.folioCount} Holdings"
                }

                accountAdapter.submitList(state.accountSummaries)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
