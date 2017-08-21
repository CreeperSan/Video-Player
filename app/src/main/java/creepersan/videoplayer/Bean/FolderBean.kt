package creepersan.videoplayer.Bean

import java.io.Serializable

class FolderBean(var folderName:String,var folderPath:String):Serializable{
    var videoList = ArrayList<VideoBean>()

}