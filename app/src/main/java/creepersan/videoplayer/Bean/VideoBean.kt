package creepersan.videoplayer.Bean

import android.database.Cursor
import android.provider.MediaStore
import java.io.Serializable

class VideoBean:Serializable{
    var name:String private set
    var folderName:String private set
    var videoPath:String private set
    var duration:String private set
    var videoWidth:Int private set
    var videoHeight:Int private set
    var size:Int private set

    constructor(name:String,folderName:String,videoPath:String,duration:String,width:Int,height:Int,size:Int){
        this.name = name
        this.folderName = folderName
        this.videoPath = videoPath
        this.duration = duration
        this.videoWidth = width
        this.videoHeight = height
        this.size = size
    }
    constructor(cursor:Cursor){
        name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
        folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
        videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
        videoWidth = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH))
        videoHeight = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT))
        size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
        try {
            duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
        } catch (e: Exception) {
            duration = "Unknown"
        }
    }
}
