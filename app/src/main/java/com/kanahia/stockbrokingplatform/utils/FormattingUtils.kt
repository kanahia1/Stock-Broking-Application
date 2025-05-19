package com.kanahia.stockbrokingplatform.utils

import java.text.DecimalFormat


fun Double.formatAsCurrency(): String {
    val format = DecimalFormat("$#,##0.00")
    return format.format(this)
}

fun Double.formatAsPercentage(): String {
    val format = DecimalFormat("0.00%")
    return format.format(this / 100)
}

fun Double.formatToMillionsOrBillions(): String {
    return when {
        this >= 1_000_000_000_000 -> {
            val trillions = this / 1_000_000_000_000
            "$${DecimalFormat("#,##0.00").format(trillions)}T"
        }

        this >= 1_000_000_000 -> {
            val billions = this / 1_000_000_000
            "$${DecimalFormat("#,##0.00").format(billions)}B"
        }

        this >= 1_000_000 -> {
            val millions = this / 1_000_000
            "$${DecimalFormat("#,##0.00").format(millions)}M"
        }

        else -> {
            DecimalFormat("$#,##0.00").format(this)
        }
    }
}

fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) this
    else "${this.substring(0, maxLength)}..."
}