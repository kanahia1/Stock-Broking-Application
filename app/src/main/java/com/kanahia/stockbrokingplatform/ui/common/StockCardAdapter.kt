package com.kanahia.stockbrokingplatform.ui.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanahia.stockbrokingplatform.databinding.ItemStockCardBinding
import com.kanahia.stockbrokingplatform.domain.model.StockCategory
import com.kanahia.stockbrokingplatform.domain.model.StockChartData
import com.kanahia.stockbrokingplatform.domain.model.StockModel
import com.kanahia.stockbrokingplatform.utils.formatToMillionsOrBillions
import com.yabu.livechart.model.DataPoint
import com.yabu.livechart.model.Dataset
import com.yabu.livechart.view.LiveChartStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class StockCardAdapter(
    private val onItemClick: (StockModel) -> Unit,
    private val showChart: Boolean = false,
    private val loadChartData: (String) -> Unit = {},
    private val getChartData: (String) -> StockChartData? = { null }
) : ListAdapter<StockModel, StockCardAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemStockCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(stock: StockModel) {
            // Set stock ticker and first letter
            binding.tvStockName.text = stock.ticker
            binding.tvCompanyName.text = "Volume: "+ ( stock.volume.toDoubleOrNull()
                ?.formatToMillionsOrBillions()
                ?: stock.volume)
            binding.tvStockSymbol.text = stock.ticker.first().toString()

            // Set stock price
            binding.tvStockPrice.text = "$"+stock.price

            val isPositive = (stock.category == StockCategory.GAINER)
            val changeText = "$"+stock.changeAmount+"("+stock.changePercentage+")"

            binding.tvPercentChange.text = if (isPositive) changeText else "-$changeText"
            binding.tvPercentChange.setTextColor(
                if (isPositive) Color.parseColor("#3A9D5D") else Color.parseColor("#E53935")
            )
            if (showChart){
                binding.stockChart.visibility = View.VISIBLE
                binding.tvPercentChange.visibility = View.INVISIBLE
                loadChartForStock(stock.ticker)
            }else{
                binding.stockChart.visibility = View.INVISIBLE
                binding.tvPercentChange.visibility = View.VISIBLE
            }

        }

        private fun loadChartForStock(ticker: String) {
            CoroutineScope(Dispatchers.IO).launch {
                // Request chart data to be loaded
                loadChartData(ticker)

                // Wait a bit for data to be loaded
                withContext(Dispatchers.Main) {
                    // Try to get the chart data
                    val chartData = getChartData(ticker)
                    chartData?.let { updateChart(it) }
                }
            }
        }

        private fun updateChart(chartData: StockChartData) {
            val dataPoints = chartData.closeValues.mapIndexed { index, value ->
                DataPoint(index.toFloat(), value)
            }

            val dataset = Dataset(dataPoints.toMutableList())

            if (dataset.points.last().y - dataset.points.first().y > 0) {
                val style = LiveChartStyle().apply {
                    textColor = Color.BLACK
                    mainColor = Color.parseColor("#3A9D5D")
                    pathStrokeWidth = 2f
                }

                binding.stockChart.setDataset(dataset)
                    .setLiveChartStyle(style)
                    .drawSmoothPath()
                    .drawDataset()
            } else {
                val style = LiveChartStyle().apply {
                    textColor = Color.BLACK
                    mainColor = Color.parseColor("#E53935")
                    pathStrokeWidth = 2f
                }

                binding.stockChart.setDataset(dataset)
                    .setLiveChartStyle(style)
                    .drawSmoothPath()
                    .drawDataset()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StockModel>() {
            override fun areItemsTheSame(oldItem: StockModel, newItem: StockModel): Boolean {
                return oldItem.ticker == newItem.ticker
            }

            override fun areContentsTheSame(oldItem: StockModel, newItem: StockModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}