package com.investtrack.ui.securities

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.investtrack.data.models.Security
import com.investtrack.data.models.SecurityType
import com.investtrack.data.repository.InvestmentRepository
import com.investtrack.databinding.FragmentAddSecurityBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSecurityViewModel @Inject constructor(
    private val repository: InvestmentRepository
) : ViewModel() {
    fun saveSecurity(security: Security) {
        viewModelScope.launch { repository.insertSecurity(security) }
    }
}

@AndroidEntryPoint
class AddSecurityFragment : Fragment() {

    private var _binding: FragmentAddSecurityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddSecurityViewModel by viewModels()
    private var selectedType = SecurityType.EQUITY

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val types = SecurityType.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.actvSecurityType.setAdapter(adapter)
        binding.actvSecurityType.setText(types[1], false) // Default EQUITY

        binding.actvSecurityType.setOnItemClickListener { _, _, position, _ ->
            selectedType = SecurityType.values()[position]
            updateFieldVisibility()
        }

        updateFieldVisibility()

        binding.btnSave.setOnClickListener { saveSecurity() }
    }

    private fun updateFieldVisibility() {
        val isFD = selectedType == SecurityType.FIXED_DEPOSIT || selectedType == SecurityType.BOND
        val isMF = selectedType == SecurityType.MUTUAL_FUND || selectedType == SecurityType.ETF
        binding.tilInterestRate.visibility = if (isFD) View.VISIBLE else View.GONE
        binding.tilMaturityDate.visibility = if (isFD) View.VISIBLE else View.GONE
        binding.tilAmc.visibility = if (isMF) View.VISIBLE else View.GONE
        binding.tilCategory.visibility = if (isMF) View.VISIBLE else View.GONE
        binding.tilExchange.visibility = if (!isFD) View.VISIBLE else View.GONE
    }

    private fun saveSecurity() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) { binding.tilName.error = "Name required"; return }
        val symbol = binding.etSymbol.text.toString().trim()
        val price = binding.etCurrentPrice.text.toString().toDoubleOrNull() ?: 0.0
        val isin = binding.etIsin.text.toString().trim()

        val security = Security(
            name = name,
            symbol = symbol,
            type = selectedType,
            isin = isin,
            exchange = binding.etExchange.text.toString().trim(),
            currentPrice = price,
            interestRate = binding.etInterestRate.text.toString().toDoubleOrNull(),
            amc = binding.etAmc.text.toString().trim(),
            category = binding.etCategory.text.toString().trim(),
            sector = binding.etSector.text.toString().trim()
        )
        viewModel.saveSecurity(security)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
