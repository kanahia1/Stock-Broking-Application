package com.kanahia.stockbrokingplatform.ui.details.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kanahia.stockbrokingplatform.R // Import your R file
import com.kanahia.stockbrokingplatform.databinding.FragmentSummaryBinding
import com.kanahia.stockbrokingplatform.domain.model.StockDetailModel
import com.kanahia.stockbrokingplatform.ui.details.viewmodel.StockDetailViewModel
import kotlinx.coroutines.launch

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockDetailViewModel by activityViewModels()

    private var isExpanded = false
    private var fullDescriptionText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stockDetails.collect { stockDetail ->
                stockDetail?.let {
                    fullDescriptionText = it.description // Store the full description
                    updateUI(it)
                    setupReadMoreTextView(it.description)
                }
            }
        }

        binding.tvReadMore.setOnClickListener {
            toggleReadMore()
        }
    }

    private fun updateUI(stockDetail: StockDetailModel) {
        with(binding) {
            tvAboutTitle.text = "About " + stockDetail.name
            // Initial setup for description will be handled by setupReadMoreTextView
            // and toggleReadMore. If not using post, set initial truncated text here.
            chipType.text = "Type: " + stockDetail.assetType
            chipSector.text = "Sector: " + stockDetail.sector
            chipIndustry.text = "Industry: " + stockDetail.industry
        }
    }

    private fun setupReadMoreTextView(description: String) {
        binding.tvAboutDescription.text = description // Set the full text first to measure

        // Post a runnable to ensure the TextView has been laid out and measured
        binding.tvAboutDescription.post {
            if (binding.tvAboutDescription.lineCount > 2) {
                binding.tvAboutDescription.maxLines = 2
                binding.tvAboutDescription.ellipsize = TextUtils.TruncateAt.END
                binding.tvReadMore.visibility = View.VISIBLE
                binding.tvReadMore.text = "read more" // Using string resource
                isExpanded = false
            } else {
                binding.tvReadMore.visibility = View.GONE
            }
        }
    }

    private fun toggleReadMore() {
        isExpanded = !isExpanded
        if (isExpanded) {
            binding.tvAboutDescription.maxLines = Integer.MAX_VALUE
            binding.tvAboutDescription.ellipsize = null
            binding.tvReadMore.text = "show less" // Using string resource
        } else {
            binding.tvAboutDescription.maxLines = 2
            binding.tvAboutDescription.ellipsize = TextUtils.TruncateAt.END
            binding.tvReadMore.text = "read more" // Using string resource
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}