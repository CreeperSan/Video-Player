package creepersan.videoplayer.Service

import android.media.MediaPlayer
import creepersan.videoplayer.Base.BaseService
import creepersan.videoplayer.Event.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MediaPlayerService : BaseService() {



    private val mediaPlayer = MediaPlayer()

    override fun onCreate() {
        super.onCreate()
    }



    @Subscribe(sticky = true)
    fun onPlayVideoEvent(event:MediaPlayVideoEvent){
        removeStickEvent(event)
        mediaPlayer.setDataSource(event.path)
        mediaPlayer.prepare()
        mediaPlayer.start()
        postEvent(MediaProgressInitResultEvent(mediaPlayer.currentPosition,mediaPlayer.duration))
    }
    @Subscribe()
    fun onMediaProgressEvent(event: MediaProgressEvent){
        postEvent(MediaProgressResultEvent(mediaPlayer.currentPosition))
    }
    @Subscribe
    fun onMediaPlayEvent(event:MediaPlayEvent){
        if(!mediaPlayer.isPlaying)
            mediaPlayer.start()
    }
    @Subscribe
    fun onMediaPlayerEvent(event:MediaPauseEvent){
        if(mediaPlayer.isPlaying)
            mediaPlayer.pause()
    }
    @Subscribe
    fun onMediaPlayOrPauseEvent(event: MediaPlayOrPauseEvent){
        if (mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }else{
            mediaPlayer.start()
        }
        postEvent(MediaPlayOrPauseResultEvent(mediaPlayer.isPlaying))
    }
    @Subscribe
    fun onMediaProgressInitEvent(event: MediaProgressInitEvent){
        postEvent(MediaProgressInitResultEvent(mediaPlayer.currentPosition,mediaPlayer.duration))
    }

    @Subscribe(sticky = true)
    fun onPlayGetMediaPlayerEvent(event: PlayGetPlayerEvent){
        removeStickEvent(event)
        postEvent(PlayGetPlayerReturnEvent(mediaPlayer))
    }
    @Subscribe
    fun onServiceShutdownEvent(event:ServiceShutdownEvent){
        mediaPlayer.release()
        stopSelf()
    }

    fun removeStickEvent(event:Any){
        EventBus.getDefault().removeStickyEvent(event)
    }


}