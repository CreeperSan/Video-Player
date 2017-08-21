package creepersan.videoplayer.Helper

import android.content.Intent
import creepersan.videoplayer.Bean.FolderBean
import creepersan.videoplayer.Bean.VideoBean

object IntentHelper {

    fun makePlayIntent(intent: Intent,filePath: String,videoName:String,videoBean: VideoBean,folderBean: FolderBean):Intent{
        intent.putExtra(IntentKey.FILE_PATH,filePath)
        intent.putExtra(IntentKey.FILE_NAME,videoName)
        intent.putExtra(IntentKey.IS_OUTSIDE,false)
        intent.putExtra(IntentKey.VIDEO_BEAN,videoBean)
        intent.putExtra(IntentKey.FOLDER_BEAN,folderBean)
        return intent
    }

    fun parseIntent(intent: Intent):BaseIntentInfo{
        if (intent.hasExtra(IntentKey.FILE_PATH)){
            return PlayIntentInfo(intent.getStringExtra(IntentKey.FILE_PATH),
                    intent.getStringExtra(IntentKey.FILE_NAME),
                    intent.getSerializableExtra(IntentKey.VIDEO_BEAN) as VideoBean,
                    intent.getBooleanExtra(IntentKey.IS_OUTSIDE,true),
                    intent.getSerializableExtra(IntentKey.FOLDER_BEAN) as FolderBean)
        }else{
            return UnknownIntentInfo(true)
        }
    }

}

object IntentKey{
    const val FILE_PATH = "FilePath"
    const val FILE_NAME = "FileName"
    const val IS_OUTSIDE = "IsOutside"
    const val VIDEO_BEAN = "VideoBean"
    const val FOLDER_BEAN = "FolderBean"
}

open class BaseIntentInfo(var isFromOutSide:Boolean)
class UnknownIntentInfo(isFromOutSide: Boolean) : BaseIntentInfo(isFromOutSide)
class PlayIntentInfo(var filePath: String,var videoName:String,var videoBean: VideoBean,isFromOutSide:Boolean,var folderBean: FolderBean) : BaseIntentInfo(isFromOutSide)


