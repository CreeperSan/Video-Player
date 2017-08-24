package creepersan.videoplayer.Base

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import butterknife.ButterKnife
import creepersan.videoplayer.Helper.CommandHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


abstract class BaseActivity : AppCompatActivity(){
    protected var TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
        ButterKnife.bind(this)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommandEvent(command:String){
        if (command == CommandHelper.COMMAND_EXIT){
            finish()
        }
    }

    abstract protected fun getLayoutID():Int;


    protected fun startActivity(cls: Class<*>,isFinish:Boolean) {
        startActivity(Intent(this,cls))
        if (isFinish) finish()
    }
    protected fun startActivity(cls:Class<*>) = startActivity(cls,false)

    protected fun startService(cls:Class<*>){
        startService(Intent(this,cls))
    }

    protected fun log(content:String) = Log.i(TAG,content)
    protected fun logV(content:String) = Log.v(TAG,content)
    protected fun logD(content:String) = Log.d(TAG,content)
    protected fun logW(content:String) = Log.w(TAG,content)
    protected fun logE(content:String) = Log.e(TAG,content)

    protected fun toast(resID:Int) = Toast.makeText(this,getString(resID),Toast.LENGTH_SHORT).show()
    protected fun toast(content: String) = Toast.makeText(this,content,Toast.LENGTH_SHORT).show()
    protected fun toastLong(content:String) = Toast.makeText(this,content,Toast.LENGTH_LONG).show()

    protected fun postEvent(event:Any) = EventBus.getDefault().post(event)
    protected fun postStickyEvent(event:Any) = EventBus.getDefault().postSticky(event)
}