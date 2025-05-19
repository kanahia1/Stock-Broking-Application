package com.kanahia.stockbrokingplatform.ui.search.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanahia.stockbrokingplatform.data.model.StockSearchResult
import com.kanahia.stockbrokingplatform.databinding.ItemSearchResultBinding

class SearchResultAdapter(
    private val onItemClick: (StockSearchResult) -> Unit
) : ListAdapter<StockSearchResult, SearchResultAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchResultViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: StockSearchResult) {
            binding.tvStockAvatar.text = item.symbol.uppercase().first().toString()
            binding.tvSymbol.text = item.symbol
            binding.tvCompanyName.text = item.name
        }
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<StockSearchResult>() {
        override fun areItemsTheSame(oldItem: StockSearchResult, newItem: StockSearchResult): Boolean {
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: StockSearchResult, newItem: StockSearchResult): Boolean {
            return oldItem == newItem
        }
    }
}