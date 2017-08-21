package creepersan.videoplayer.Event

import android.media.MediaPlayer
import creepersan.videoplayer.Activity.PlayActivity
import creepersan.videoplayer.Bean.FolderBean

class ActivityOnlineEvent(val playActivity: PlayActivity,var videoPath:String)
class ActivityOffLineEvent(val playActivity: PlayActivity)

class MediaPlayVideoEvent(var path:String)
class MediaPlayEvent()
class MediaPauseEvent()
class MediaProgressEvent()
class MediaPlayOrPauseEvent()
class MediaPlayOrPauseResultEvent(var isPlaying:Boolean)
class MediaProgressResultEvent(val current:Int)
class MediaProgressInitEvent()
class MediaProgressInitResultEvent(val current:Int, val total:Int)

class PlayGetPlayerEvent()
class PlayGetPlayerReturnEvent(val mediaPlayer:MediaPlayer)
class ServiceShutdownEvent()

