package creepersan.videoplayer.Service

import android.media.MediaPlayer
import creepersan.videoplayer.Base.BaseService
import creepersan.videoplayer.Event.ActivityOnlineEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ManageService : BaseService() {



    private val mediaPlayer = MediaPlayer()

    override fun onCreate() {
        super.onCreate()
    }



    fun removeStickEvent(event:Any){
        EventBus.getDefault().removeStickyEvent(event)
    }


}