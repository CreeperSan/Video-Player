package creepersan.videoplayer.Helper

import java.math.BigDecimal
import java.text.DecimalFormat

object NumberHelper {

    private val decimalFormat = DecimalFormat("#.00")

    fun getFileSizeFromB_MB(size:Int):String = decimalFormat.format(size.toFloat()/1024f/1024f)

}