package com.kanahia.stockbrokingplatform.ui.details

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.kanahia.stockbrokingplatform.R
import com.kanahia.stockbrokingplatform.databinding.ActivityDetailsBinding
import com.kanahia.stockbrokingplatform.domain.model.StockChartData
import com.kanahia.stockbrokingplatform.domain.model.StockDetailModel
import com.kanahia.stockbrokingplatform.ui.details.adapter.ViewPagerAdapter
import com.kanahia.stockbrokingplatform.ui.details.viewmodel.StockDetailViewModel
import com.kanahia.stockbrokingplatform.utils.Constants
import com.kanahia.stockbrokingplatform.utils.formatAsCurrency
import com.kanahia.stockbrokingplatform.utils.formatAsPercentage
import com.kanahia.stockbrokingplatform.utils.formatToMillionsOrBillions
import com.yabu.livechart.model.DataPoint
import com.yabu.livechart.model.Dataset
import com.yabu.livechart.view.LiveChartStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private val viewModel: StockDetailViewModel by viewModels()
    private var stockSymbol: String = "IBM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewPager()
        setupChipListeners()
        setupViewPagerListener()
        observeStockDetails()
        setupChartButtons()
        setupWishlistButton()
        setupLoadingState()

        stockSymbol = intent.getStringExtra(Constants.STOCK) ?: "IBM"
        viewModel.loadStockDetails(stockSymbol)
    }

    private fun setupLoadingState() {
        showLoading(true)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingContainer.visibility = View.VISIBLE
            binding.contentContainer.visibility = View.GONE
        } else {
            binding.loadingContainer.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
        }
    }

    private fun observeStockDetails() {
        lifecycleScope.launch {
            viewModel.stockDetails.collect { stockDetail ->
                stockDetail?.let {
                    updateUI(it)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.chartData.collectLatest { chartData ->
                chartData?.let { updateChart(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.chartTimeframe.collectLatest { timeframe ->
                updateSelectedButton(timeframe)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                showLoading(isLoading)
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMessage ->
                errorMessage?.let {
                    showLoading(false)
                     Toast.makeText(this@DetailsActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateSelectedButton(timeframe: String) {
        when (timeframe) {
            "1D" -> binding.timeFrameChipGroup.check(R.id.chip_1d)
            "1W" -> binding.timeFrameChipGroup.check(R.id.chip_7d)
            "1M" -> binding.timeFrameChipGroup.check(R.id.chip_1m)
            "3M" -> binding.timeFrameChipGroup.check(R.id.chip_3m)
            "6M" -> binding.timeFrameChipGroup.check(R.id.chip_6m)
            "1Y" -> binding.timeFrameChipGroup.check(R.id.chip_1y)
        }

    }

    private fun updateChart(chartData: StockChartData) {
        val dataPoints = chartData.closeValues.mapIndexed { index, value ->
            DataPoint(index.toFloat(), value)
        }

        val dataset = Dataset(dataPoints.toMutableList())

        if (dataset.points.last().y - dataset.points.first().y > 0){
            val style = LiveChartStyle().apply {
                textColor = Color.BLACK
                textHeight = 30f
                mainColor = Color.GREEN
                baselineColor = Color.BLACK
                pathStrokeWidth = 6f
                baselineStrokeWidth = 4f
            }

            binding.stockChart.setDataset(dataset)
                .setLiveChartStyle(style)
                .drawYBounds()
                .drawBaseline()
                .drawFill(true)
                .drawSmoothPath()
                .drawVerticalGuidelines(4)
                .drawHorizontalGuidelines(4)
                .drawDataset()

        }else{
            val style = LiveChartStyle().apply {
                textColor = Color.BLACK
                textHeight = 30f
                mainColor = Color.RED
                baselineColor = Color.BLACK
                pathStrokeWidth = 6f
                baselineStrokeWidth = 4f
            }

            binding.stockChart.setDataset(dataset)
                .setLiveChartStyle(style)
                .drawYBounds()
                .drawBaseline()
                .drawFill(true)
                .drawSmoothPath()
                .drawVerticalGuidelines(4)
                .drawHorizontalGuidelines(4)
                .drawDataset()
        }
    }

    private fun updateUI(stockDetail: StockDetailModel) {
        with(binding) {
            tvCompanyName.text = stockDetail.name
            tvStockSymbol.text = "${stockDetail.symbol}, ${stockDetail.assetType}"
            tvStockAvatar.text = stockDetail.symbol.uppercase()[0].toString()
            tvStockPrice.text = stockDetail.price.formatAsCurrency()
            if (!stockDetail.isPositive){
                val amountChange = stockDetail.priceChange.formatAsCurrency() + "(" + stockDetail.priceChangePercent + ")"
                tvPriceChange.text = amountChange
                tvPriceChange.setTextColor(Color.RED)
            }else{
                val amountChange = stockDetail.priceChange.formatAsCurrency() + "(" + stockDetail.priceChangePercent + ")"
                tvPriceChange.text = amountChange
                tvPriceChange.setTextColor(Color.GREEN)
            }
            openTv.text = stockDetail.open.formatAsCurrency()
            dayLowTv.text = stockDetail.dayLow.formatAsCurrency()
            dayHighTv.text = stockDetail.dayHigh.formatAsCurrency()
            fiftyTwoLowTv.text = stockDetail.weekLow52.formatAsCurrency()
            fiftyTwoHighTv.text = stockDetail.weekHigh52.formatAsCurrency()

            priceProgressBar.currentPrice = stockDetail.price.toFloat()
            priceProgressBar.maxPrice = stockDetail.weekHigh52.toFloat()
            priceProgressBar.minPrice = stockDetail.weekLow52.toFloat()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupChartButtons() {
        binding.chip1d.setOnClickListener {
            stockSymbol?.let { viewModel.loadChartData(it, "1D") }
        }

        binding.chip7d.setOnClickListener {
            stockSymbol?.let { viewModel.loadChartData(it, "1W") }
        }

        binding.chip1m.setOnClickListener {
            stockSymbol?.let { viewModel.loadChartData(it, "1M") }
        }

        binding.chip3m.setOnClickListener {
            stockSymbol?.let { viewModel.loadChartData(it, "3M") }
        }

        binding.chip6m.setOnClickListener {
            stockSymbol?.let { viewModel.loadChartData(it, "6M") }
        }

        binding.chip1y.setOnClickListener {
            stockSymbol?.let { viewModel.loadChartData(it, "1Y") }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = ViewPagerAdapter(this)
    }

    private fun setupChipListeners() {
        binding.chipSummary.setOnClickListener {
            binding.viewPager.currentItem = 0
        }

        binding.chipDetails.setOnClickListener {
            binding.viewPager.currentItem = 1
        }

        binding.chipNews.setOnClickListener {
            binding.viewPager.currentItem = 2
        }
    }

    private fun setupViewPagerListener() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.detailsFrameChipGroup.check(R.id.chip_summary)
                    1 -> binding.detailsFrameChipGroup.check(R.id.chip_details)
                    2 -> binding.detailsFrameChipGroup.check(R.id.chip_news)
                }
            }
        })
    }

    private fun setupWishlistButton() {
        binding.wishlistButton.setOnClickListener {
            viewModel.toggleWishlistStatus()
        }

        lifecycleScope.launch {
            viewModel.isWishlisted.collect { isWishlisted ->
                if (isWishlisted) {
                    binding.wishlistButton.setImageResource(R.drawable.ic_check_circle)
                } else {
                    binding.wishlistButton.setImageResource(R.drawable.ic_add_wishlist)
                }
            }
        }
    }
}