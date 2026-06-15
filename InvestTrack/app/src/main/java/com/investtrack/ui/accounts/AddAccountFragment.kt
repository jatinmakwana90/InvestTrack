package com.investtrack.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.investtrack.data.models.Account
import com.investtrack.data.models.AccountType
import com.investtrack.data.repository.InvestmentRepository
import com.investtrack.databinding.FragmentAddAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val repository: InvestmentRepository
) : ViewModel() {
    fun saveAccount(name: String, type: AccountType, description: String, currency: String) {
        viewModelScope.launch {
            repository.insertAccount(Account(name = name, type = type, description = description, currency = currency))
        }
    }
}

@AndroidEntryPoint
class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddAccountViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val accountTypes = AccountType.values().map { it.name.replace("_", " ") }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accountTypes)
        binding.actvAccountType.setAdapter(adapter)
        binding.actvAccountType.setText(accountTypes[0], false)

        val currencies = listOf("INR", "USD", "EUR", "GBP")
        val currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        binding.actvCurrency.setAdapter(currencyAdapter)
        binding.actvCurrency.setText(currencies[0], false)

        binding.btnSave.setOnClickListener {
            val name = binding.etAccountName.text.toString().trim()
            if (name.isEmpty()) {
                binding.tilAccountName.error = "Name required"
                return@setOnClickListener
            }
            val typeStr = binding.actvAccountType.text.toString().replace(" ", "_")
            val type = AccountType.valueOf(typeStr)
            val desc = binding.etDescription.text.toString().trim()
            val currency = binding.actvCurrency.text.toString()
            viewModel.saveAccount(name, type, desc, currency)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
