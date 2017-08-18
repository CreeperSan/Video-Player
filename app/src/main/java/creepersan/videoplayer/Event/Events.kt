package creepersan.videoplayer.Event

import creepersan.videoplayer.Activity.PlayActivity
import creepersan.videoplayer.Bean.FolderBean

class ActivityOnlineEvent(val playActivity: PlayActivity,var videoPath:String)
class ActivityOffLineEvent(val playActivity: PlayActivity)
class VideoProgressEvent(val current:Int, val total:Int)
