package com.investtrack.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.investtrack.data.models.Folio
import com.investtrack.data.models.Security
import com.investtrack.data.repository.InvestmentRepository
import com.investtrack.databinding.FragmentFolioDetailBinding
import com.investtrack.utils.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolioDetailViewModel @Inject constructor(
    private val repository: InvestmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val accountId: Long = savedStateHandle.get<Long>("accountId") ?: 0L
    val securityId: Long = savedStateHandle.get<Long>("securityId") ?: 0L

    private val _uiState = MutableStateFlow(FolioDetailUiState())
    val uiState: StateFlow<FolioDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val security = repository.getSecurityById(securityId)
                val folios = repository.getFoliosForAccount(accountId)
                val folio = folios.firstOrNull { it.securityId == securityId }

                repository.getTransactionsByFolio(accountId, securityId).collect { txns ->
                    val items = txns.map { TransactionItem(it, security) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            security = security,
                            folio = folio,
                            transactions = items
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

data class FolioDetailUiState(
    val isLoading: Boolean = false,
    val security: Security? = null,
    val folio: Folio? = null,
    val transactions: List<TransactionItem> = emptyList(),
    val error: String? = null
)

@AndroidEntryPoint
class FolioDetailFragment : Fragment() {

    private var _binding: FragmentFolioDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FolioDetailViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFolioDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        adapter = TransactionAdapter()
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@FolioDetailFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.security?.let { sec ->
                    binding.toolbar.title = sec.name
                    binding.tvSecurityName.text = sec.name
                    binding.tvSecurityType.text = sec.type.displayName
                    binding.tvSymbol.text = sec.symbol.ifEmpty { sec.isin }
                    binding.tvCmp.text = "CMP: ${CurrencyFormatter.format(sec.currentPrice)}"
                }
                state.folio?.let { folio ->
                    binding.tvCurrentValue.text = CurrencyFormatter.format(folio.currentValue)
                    binding.tvInvested.text = "Invested: ${CurrencyFormatter.format(folio.totalInvested)}"
                    val pnl = folio.pnl
                    binding.tvPnL.text = "${if (pnl >= 0) "+" else ""}${CurrencyFormatter.format(pnl)} (${String.format("%.2f", folio.pnlPercent)}%)"
                    binding.tvPnL.setTextColor(
                        requireContext().getColor(if (pnl >= 0) com.investtrack.R.color.gain_green else com.investtrack.R.color.loss_red)
                    )
                    binding.tvUnits.text = String.format("%.3f", folio.totalUnits)
                    binding.tvAvgCost.text = CurrencyFormatter.format(folio.avgCostPrice)
                    if (folio.folioNumber.isNotEmpty()) {
                        binding.tvFolioNo.text = "Folio: ${folio.folioNumber}"
                        binding.tvFolioNo.visibility = View.VISIBLE
                    }
                }
                adapter.submitList(state.transactions)
                binding.tvTxnCount.text = "${state.transactions.size} Transactions"
            }
        }
    }

    private val lifecycleScope get() = viewLifecycleOwner.lifecycleScope

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
