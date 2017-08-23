package creepersan.videoplayer.Helper

import android.content.Context
import android.content.SharedPreferences
import java.security.Key

class PrefHelper private constructor(context: Context) {
    private val prefConf:SharedPreferences

    init {
        prefConf = context.applicationContext.getSharedPreferences(NAME.PREF_CONF,Context.MODE_PRIVATE)
    }


    private object SELF{
        internal var helper:PrefHelper? = null
    }

    companion object {
        fun getPrefHelper(context: Context):PrefHelper{
            if (SELF.helper ==null){
                SELF.helper = PrefHelper(context.applicationContext)
            }
            return SELF.helper as PrefHelper
        }
        //通用获取方法
        fun getStringConfig(key:String,default:String,context: Context):String = getPrefHelper(context).prefConf.getString(key,default)
        fun getIntConfig(key:String,default:Int,context: Context):Int = getPrefHelper(context).prefConf.getInt(key,default)
        fun getBooleanConfig(key:String,default:Boolean,context: Context):Boolean = getPrefHelper(context).prefConf.getBoolean(key,default)
        fun getFloatConfig(key:String,default:Float,context: Context):Float = getPrefHelper(context).prefConf.getFloat(key,default)
        fun getLongConfig(key:String,default:Long,context: Context):Long = getPrefHelper(context).prefConf.getLong(key,default)
        //通用设置方法
        fun setConfig(key: String,value:String,context: Context) = getPrefHelper(context).prefConf.edit().putString(key,value).commit()
        fun setConfig(key: String,value:Int,context: Context) = getPrefHelper(context).prefConf.edit().putInt(key,value).commit()
        fun setConfig(key: String,value:Boolean,context: Context) = getPrefHelper(context).prefConf.edit().putBoolean(key,value).commit()
        fun setConfig(key: String,value:Float,context: Context) = getPrefHelper(context).prefConf.edit().putFloat(key,value).commit()
        fun setConfig(key: String,value:Long,context: Context) = getPrefHelper(context).prefConf.edit().putLong(key,value).commit()
        //专用取值与写值
        fun getAlwaysOnInfoStatus(context: Context):Boolean = getBooleanConfig(KEY.ALWAYS_ON_INFO,false,context)
        fun getAlwaysOnProgressStatus(context: Context):Boolean = getBooleanConfig(KEY.ALWAYS_ON_PROGRESS,false,context)
        fun getGestureDoubleTapStatus(context: Context):Boolean = getBooleanConfig(KEY.GESTURE_DOUBLE_TAP,true,context)
        fun getGestureProgressStatus(context: Context):Boolean = getBooleanConfig(KEY.GESTURE_PROGRESS,true,context)
        fun getGestureVolumeStatus(context: Context):Boolean = getBooleanConfig(KEY.GESTURE_VOLUME,true,context)
        fun getGestureBrightnessStatus(context: Context):Boolean = getBooleanConfig(KEY.GESTURE_BRIGHTNESS,true,context)

        fun setAlwaysOnInfoStatus(status:Boolean,context: Context) = setConfig(KEY.ALWAYS_ON_INFO,status,context)
        fun setAlwaysOnProgressStatus(status: Boolean,context: Context) = setConfig(KEY.ALWAYS_ON_PROGRESS,status,context)
        fun setGestureDoubleTapStatus(status: Boolean,context: Context) = setConfig(KEY.GESTURE_DOUBLE_TAP,status,context)
        fun setGestureProgressStatus(status: Boolean,context: Context) = setConfig(KEY.GESTURE_PROGRESS,status,context)
        fun setGestureVolumeStatus(status: Boolean,context: Context) = setConfig(KEY.GESTURE_VOLUME,status,context)
        fun setGestureBrightnessStatus(status: Boolean,context: Context) = setConfig(KEY.GESTURE_BRIGHTNESS,status,context)

    }

    object KEY{
        val ALWAYS_ON_INFO = "prefCatalogDisplayAlwaysOnInfo"
        val ALWAYS_ON_PROGRESS = "prefCatalogDisplayAlwaysOnProgress"
        val GESTURE_DOUBLE_TAP = "prefCatalogGestureDoubleTapPlayOrResume"
        val GESTURE_PROGRESS = "prefCatalogGestureProgress"
        val GESTURE_VOLUME = "prefCatalogGestureVolume"
        val GESTURE_BRIGHTNESS = "prefCatalogGestureBrightness"
    }

    object NAME{
        val PREF_CONF = "Config"
    }
}