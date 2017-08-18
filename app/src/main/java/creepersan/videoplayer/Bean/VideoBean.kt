package creepersan.videoplayer.Bean

import android.database.Cursor
import android.provider.MediaStore
import java.io.Serializable

class VideoBean:Serializable{
    var name:String private set
    var folderName:String private set
    var videoPath:String private set
    var duration:String private set

    constructor(name:String,folderName:String,videoPath:String,description:String,duration:String,language:String,tags:String){
        this.name = name
        this.folderName = folderName
        this.videoPath = videoPath
        this.duration = duration
    }
    constructor(cursor:Cursor){
        name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
        folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
        videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
        try {
            duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
        } catch (e: Exception) {
            duration = "Unknown"
        }
    }
}
