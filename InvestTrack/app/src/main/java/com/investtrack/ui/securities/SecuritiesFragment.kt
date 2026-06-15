package com.investtrack.ui.securities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.investtrack.data.models.Security
import com.investtrack.data.models.SecurityType
import com.investtrack.data.repository.InvestmentRepository
import com.investtrack.databinding.FragmentSecuritiesBinding
import com.investtrack.databinding.FragmentAddSecurityBinding
import com.investtrack.databinding.ItemSecurityBinding
import com.investtrack.utils.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritiesViewModel @Inject constructor(
    private val repository: InvestmentRepository
) : ViewModel() {

    val securities: StateFlow<List<Security>> = repository.getAllSecurities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updatePrice(id: Long, price: Double) {
        viewModelScope.launch { repository.updatePrice(id, price) }
    }

    fun deleteSecurity(id: Long) {
        viewModelScope.launch { repository.deactivateSecurity(id) }
    }
}

@AndroidEntryPoint
class SecuritiesFragment : Fragment() {

    private var _binding: FragmentSecuritiesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SecuritiesViewModel by viewModels()
    private lateinit var adapter: SecurityAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSecuritiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SecurityAdapter()
        binding.rvSecurities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@SecuritiesFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.securities.collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabAddSecurity.setOnClickListener {
            findNavController().navigate(com.investtrack.R.id.action_securities_to_addSecurity)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val all = viewModel.securities.value
                adapter.submitList(if (query.isEmpty()) all else all.filter {
                    it.name.lowercase().contains(query) || it.symbol.lowercase().contains(query)
                })
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Need lifecycle reference
    private val lifecycleScope get() = viewLifecycleOwner.lifecycleScope

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SecurityAdapter : ListAdapter<Security, SecurityAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemSecurityBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: Security) {
            b.tvName.text = s.name
            b.tvSymbol.text = if (s.symbol.isNotEmpty()) s.symbol else s.isin
            b.tvType.text = s.type.displayName
            b.tvPrice.text = CurrencyFormatter.format(s.currentPrice)
            if (s.interestRate != null) {
                b.tvExtra.text = "${s.interestRate}% p.a."
                b.tvExtra.visibility = View.VISIBLE
            } else if (s.amc.isNotEmpty()) {
                b.tvExtra.text = s.amc
                b.tvExtra.visibility = View.VISIBLE
            } else {
                b.tvExtra.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSecurityBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Security>() {
            override fun areItemsTheSame(a: Security, b: Security) = a.id == b.id
            override fun areContentsTheSame(a: Security, b: Security) = a == b
        }
    }
}
