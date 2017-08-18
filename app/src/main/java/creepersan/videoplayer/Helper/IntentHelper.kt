package creepersan.videoplayer.Helper

import android.content.Intent
import creepersan.videoplayer.Bean.VideoBean

object IntentHelper {

    fun makePlayIntent(intent: Intent,filePath: String,videoBean: VideoBean):Intent{
        intent.putExtra(IntentKey.FILE_PATH,filePath)
        intent.putExtra(IntentKey.IS_OUTSIDE,false)
        intent.putExtra(IntentKey.VIDEO_BEAN,videoBean)
        return intent
    }

    fun parseIntent(intent: Intent):BaseIntentInfo{
        if (intent.hasExtra(IntentKey.FILE_PATH)){
            return PlayIntentInfo(intent.getStringExtra(IntentKey.FILE_PATH),
                    intent.getSerializableExtra(IntentKey.VIDEO_BEAN) as VideoBean,
                    intent.getBooleanExtra(IntentKey.IS_OUTSIDE,true))
        }else{
            return UnknownIntentInfo(true)
        }
    }

}

object IntentKey{
    const val FILE_PATH = "FilePath"
    const val IS_OUTSIDE = "IsOutside"
    const val VIDEO_BEAN = "VideoBean"
}

open class BaseIntentInfo(var isFromOutSide:Boolean)
class UnknownIntentInfo(isFromOutSide: Boolean) : BaseIntentInfo(isFromOutSide)
class PlayIntentInfo(var filePath: String,var videoBean: VideoBean,isFromOutSide:Boolean) : BaseIntentInfo(isFromOutSide)


