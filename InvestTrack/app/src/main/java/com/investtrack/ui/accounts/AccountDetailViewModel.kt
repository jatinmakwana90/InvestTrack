package com.investtrack.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.investtrack.data.models.*
import com.investtrack.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val repository: InvestmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val accountId: Long = savedStateHandle.get<Long>("accountId") ?: 0L

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.FOLIO)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val account = repository.getAccountById(accountId)
                val folios = repository.getFoliosForAccount(accountId)
                val summary = repository.getAccountPortfolioSummary(accountId)

                repository.getTransactionsByAccount(accountId).collect { transactions ->
                    val enrichedTxns = transactions.map { txn ->
                        val security = repository.getSecurityById(txn.securityId)
                        TransactionItem(txn, security)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            account = account,
                            summary = summary,
                            folios = folios,
                            transactions = enrichedTxns
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setViewMode(mode: ViewMode) { _viewMode.value = mode }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}

data class AccountDetailUiState(
    val isLoading: Boolean = false,
    val account: Account? = null,
    val summary: PortfolioSummary? = null,
    val folios: List<Folio> = emptyList(),
    val transactions: List<TransactionItem> = emptyList(),
    val error: String? = null
)

data class TransactionItem(
    val transaction: Transaction,
    val security: Security?
)

enum class ViewMode { FOLIO, TRANSACTION }
