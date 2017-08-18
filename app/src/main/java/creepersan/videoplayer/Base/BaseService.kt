package creepersan.videoplayer.Base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

open class BaseService :Service() {
    protected var TAG = javaClass.simpleName

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onCommand(command:String){}

    protected fun log(content:String) = Log.i(TAG,content)
    protected fun logV(content:String) = Log.v(TAG,content)
    protected fun logD(content:String) = Log.d(TAG,content)
    protected fun logW(content:String) = Log.w(TAG,content)
    protected fun logE(content:String) = Log.e(TAG,content)

    override fun onBind(p0: Intent?): IBinder? = null

    protected fun postEvent(event:Any) = EventBus.getDefault().post(event)
    protected fun postStickyEvent(event:Any) = EventBus.getDefault().postSticky(event)
}