package creepersan.videoplayer.Helper

import android.content.Context
import creepersan.videoplayer.Activity.R
import java.text.SimpleDateFormat
import java.util.*

object TimeHelper {
    val videoDurationFormatter = SimpleDateFormat("mm:ss")
    val timeBaseFormatter = SimpleDateFormat("hh:ss")

    fun getDurationStr(timeStamp:Long,context: Context):String{
        if (timeStamp==Long.MIN_VALUE) return context.getString(R.string.mainUnknownVideoDuration)
        run {
            if (timeStamp<0){
                val newTimeStamp = Math.abs(timeStamp)
                return "-${videoDurationFormatter.format(Date(newTimeStamp))}"
            }
            return videoDurationFormatter.format(Date(timeStamp))
        }
    }
    fun getTimeStr(timeStamp: Long,context: Context):String{
        if (timeStamp==Long.MIN_VALUE) return context.getString(R.string.mainUnknownVideoDuration)
        run {
            if (timeStamp<0){
                val newTimeStamp = Math.abs(timeStamp)
                return "-${timeBaseFormatter.format(Date(newTimeStamp))}"
            }
            return timeBaseFormatter.format(Date(timeStamp))
        }
    }
}