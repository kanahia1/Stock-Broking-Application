package com.kanahia.stockbrokingplatform.ui.details.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kanahia.stockbrokingplatform.databinding.FragmentDetailsBinding
import com.kanahia.stockbrokingplatform.domain.model.StockDetailModel
import com.kanahia.stockbrokingplatform.ui.details.viewmodel.StockDetailViewModel
import com.kanahia.stockbrokingplatform.utils.formatAsPercentage
import com.kanahia.stockbrokingplatform.utils.formatToMillionsOrBillions
import kotlinx.coroutines.launch

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    // Share ViewModel with the activity
    private val viewModel: StockDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the stockDetails from the shared ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stockDetails.collect { stockDetail ->
                stockDetail?.let {
                    updateUI(it)
                }
            }
        }
    }

    private fun updateUI(stockDetail: StockDetailModel) {
        with(binding) {
             marketCapTv.text = stockDetail.marketCap.formatToMillionsOrBillions()
             peTv.text = stockDetail.peRatio.toString()
             betaTv.text = stockDetail.beta.toString()
             dividendTv.text = stockDetail.dividendYield.formatAsPercentage()
             profitTv.text = stockDetail.profitMargin.toString()
             exchangeTv.text = stockDetail.exchange.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}