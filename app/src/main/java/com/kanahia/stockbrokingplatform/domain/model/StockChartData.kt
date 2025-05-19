package com.kanahia.stockbrokingplatform.domain.model

data class StockChartData(
    val timePoints: List<String>,
    val closeValues: List<Float>,
    val openValues: List<Float>,
    val highValues: List<Float>,
    val lowValues: List<Float>,
    val volumes: List<Int>,
    val interval: String,
    val symbol: String
)
