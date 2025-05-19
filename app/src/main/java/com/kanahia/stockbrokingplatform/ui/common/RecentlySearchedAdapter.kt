package com.kanahia.stockbrokingplatform.ui.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.databinding.ItemRecentlySearchedBinding

class RecentlySearchedAdapter(
    private val onItemClick: (RecentStock) -> Unit
) : ListAdapter<RecentStock, RecentlySearchedAdapter.RecentlySearchedViewHolder>(
    RecentStockDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentlySearchedViewHolder {
        val binding = ItemRecentlySearchedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentlySearchedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentlySearchedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentlySearchedViewHolder(private val binding: ItemRecentlySearchedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: RecentStock) {
            // Set stock avatar with first letter
            binding.tvStockAvatar.text = item.symbol.first().toString()

            // Set stock symbol and company name
            binding.tvStockSymbol.text = item.symbol
            binding.tvCompanyName.text = item.name

            // Set price
            binding.tvStockPrice.text = item.price

            binding.tvStockChange.text = item.priceChange

            if (item.isNegativeChange){
                val priceChanged = item.priceChange
                priceChanged.drop(1)
                val amountChange = "-" +"$"+ priceChanged + "(" + item.changePercent + ")"
                binding.tvStockChange.text = amountChange
                binding.tvStockChange.setTextColor(Color.RED)
            }else{
                val amountChange = "+" +"$"+ item.priceChange + "(" + item.changePercent + ")"
                binding.tvStockChange.text = amountChange
                binding.tvStockChange.setTextColor(Color.GREEN)
            }
        }
    }

    class RecentStockDiffCallback : DiffUtil.ItemCallback<RecentStock>() {
        override fun areItemsTheSame(oldItem: RecentStock, newItem: RecentStock): Boolean {
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: RecentStock, newItem: RecentStock): Boolean {
            return oldItem == newItem
        }
    }
}