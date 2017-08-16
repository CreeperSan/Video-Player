package creepersan.videoplayer.Base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import butterknife.ButterKnife
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommandEvent(command:String){}

    abstract protected fun getLayoutID():Int;

    protected fun log(content:String) = Log.i(TAG,content)
    protected fun logV(content:String) = Log.v(TAG,content)
    protected fun logD(content:String) = Log.d(TAG,content)
    protected fun logW(content:String) = Log.w(TAG,content)
    protected fun logE(content:String) = Log.e(TAG,content)

    protected fun toast(content: String) = Toast.makeText(this,content,Toast.LENGTH_SHORT).show()
    protected fun toastLong(content:String) = Toast.makeText(this,content,Toast.LENGTH_LONG).show()


}