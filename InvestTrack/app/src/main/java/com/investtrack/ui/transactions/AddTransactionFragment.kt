package com.investtrack.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.investtrack.data.models.Security
import com.investtrack.data.models.Transaction
import com.investtrack.data.models.TransactionType
import com.investtrack.data.repository.InvestmentRepository
import com.investtrack.databinding.FragmentAddTransactionBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: InvestmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val accountId: Long = savedStateHandle.get<Long>("accountId") ?: 0L

    private val _securities = MutableStateFlow<List<Security>>(emptyList())
    val securities: StateFlow<List<Security>> = _securities

    init {
        viewModelScope.launch {
            repository.getAllSecurities().collect { _securities.value = it }
        }
    }

    fun saveTransaction(
        securityId: Long, type: TransactionType, qty: Double, price: Double,
        charges: Double, folioNo: String, notes: String, date: Long
    ) {
        viewModelScope.launch {
            val amount = qty * price
            repository.insertTransaction(
                Transaction(
                    accountId = accountId,
                    securityId = securityId,
                    type = type,
                    quantity = qty,
                    price = price,
                    amount = amount,
                    charges = charges,
                    folioNumber = folioNo,
                    notes = notes,
                    transactionDate = date
                )
            )
        }
    }
}

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTransactionViewModel by viewModels()
    private var selectedDate = System.currentTimeMillis()
    private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var selectedSecurityId: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.etDate.setText(dateFmt.format(Date(selectedDate)))

        // Transaction types
        val txnTypes = TransactionType.values().map { it.displayName }
        binding.actvTxnType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, txnTypes))
        binding.actvTxnType.setText(txnTypes[0], false)

        // Securities dropdown
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.securities.collect { list ->
                val names = list.map { "${it.name} (${it.type.displayName})" }
                val secAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                binding.actvSecurity.setAdapter(secAdapter)
                binding.actvSecurity.setOnItemClickListener { _, _, pos, _ ->
                    selectedSecurityId = list[pos].id
                    val sec = list[pos]
                    binding.etPrice.setText(sec.currentPrice.toString())
                }
            }
        }

        // Date picker
        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                selectedDate = cal.timeInMillis
                binding.etDate.setText(dateFmt.format(Date(selectedDate)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Auto-calculate amount
        binding.etQty.addTextChangedListener(calcAmountWatcher)
        binding.etPrice.addTextChangedListener(calcAmountWatcher)

        binding.btnSave.setOnClickListener { save() }
    }

    private val calcAmountWatcher = object : android.text.TextWatcher {
        override fun afterTextChanged(s: android.text.Editable?) {
            val qty = binding.etQty.text.toString().toDoubleOrNull() ?: return
            val price = binding.etPrice.text.toString().toDoubleOrNull() ?: return
            binding.tvCalculatedAmount.text = "Amount: ₹${String.format("%.2f", qty * price)}"
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun save() {
        if (selectedSecurityId == 0L) { binding.tilSecurity.error = "Select a security"; return }
        val qty = binding.etQty.text.toString().toDoubleOrNull()
        if (qty == null || qty <= 0) { binding.tilQty.error = "Enter valid quantity"; return }
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        if (price == null || price < 0) { binding.tilPrice.error = "Enter valid price"; return }

        val typeStr = binding.actvTxnType.text.toString()
        val type = TransactionType.values().firstOrNull { it.displayName == typeStr } ?: TransactionType.BUY
        val charges = binding.etCharges.text.toString().toDoubleOrNull() ?: 0.0
        val folioNo = binding.etFolioNo.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        viewModel.saveTransaction(selectedSecurityId, type, qty, price, charges, folioNo, notes, selectedDate)
        findNavController().navigateUp()
    }

    private val lifecycleScope get() = viewLifecycleOwner.lifecycleScope

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
