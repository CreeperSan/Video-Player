package creepersan.videoplayer.Event

import android.media.MediaPlayer
import creepersan.videoplayer.Activity.PlayActivity
import creepersan.videoplayer.Bean.FolderBean

class MediaProgressResultEvent(val current:Int)

class SettingAlwaysOnInfoEvent(val newStatus:Boolean)
class SettingAlwaysOnProgressEvent(val newStatus: Boolean)
class SettingGestureDoubleTapEvent(val newStatus: Boolean)
class SettingGestureProgressEvent(val newStatus: Boolean)
class SettingGestureBrightnessEvent(val newStatus: Boolean)
class SettingGestureVolumeEvent(val newStatus: Boolean)

