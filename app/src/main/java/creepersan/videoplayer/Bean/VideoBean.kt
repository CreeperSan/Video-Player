package creepersan.videoplayer.Bean

import android.database.Cursor
import android.provider.MediaStore

class VideoBean{
    var name:String private set
    var folderName:String private set
    var videoPath:String private set
    var description:String private set
    var duration:String private set
    var language:String private set
    var tags:String private set

    constructor(name:String,folderName:String,videoPath:String,description:String,duration:String,language:String,tags:String){
        this.name = name
        this.folderName = folderName
        this.videoPath = videoPath
        this.description = description
        this.duration = duration
        this.language = language
        this.tags = tags
    }
    constructor(cursor:Cursor){
        name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
        folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
        videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
        description = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION))
        duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
        language = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.LANGUAGE))
        tags = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TAGS))
    }
}
