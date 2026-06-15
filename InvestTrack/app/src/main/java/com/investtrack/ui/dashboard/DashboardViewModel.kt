package com.investtrack.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.investtrack.data.models.*
import com.investtrack.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: InvestmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAllAccounts().collect { accounts ->
                    val summary = repository.getPortfolioSummary()
                    val folios = repository.getAllFolios()
                    val accountSummaries = accounts.map { account ->
                        val acSummary = repository.getAccountPortfolioSummary(account.id)
                        AccountSummaryItem(account = account, summary = acSummary)
                    }
                    val byType = folios.groupBy { it.securityType }
                        .map { (type, fList) ->
                            TypeAllocation(
                                type = type,
                                value = fList.sumOf { it.currentValue },
                                invested = fList.sumOf { it.totalInvested }
                            )
                        }.sortedByDescending { it.value }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            portfolioSummary = summary,
                            accountSummaries = accountSummaries,
                            typeAllocations = byType,
                            topHoldings = folios.sortedByDescending { it.currentValue }.take(5)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val portfolioSummary: PortfolioSummary? = null,
    val accountSummaries: List<AccountSummaryItem> = emptyList(),
    val typeAllocations: List<TypeAllocation> = emptyList(),
    val topHoldings: List<Folio> = emptyList(),
    val error: String? = null
)

data class AccountSummaryItem(
    val account: Account,
    val summary: PortfolioSummary
)

data class TypeAllocation(
    val type: SecurityType,
    val value: Double,
    val invested: Double
)
