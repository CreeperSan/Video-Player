package creepersan.videoplayer.Activity

import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.util.Log
import android.view.MenuItem
import creepersan.videoplayer.Base.BaseActivity
import creepersan.videoplayer.Event.*
import creepersan.videoplayer.Helper.PrefHelper
import org.greenrobot.eventbus.EventBus

class SettingActivity : BaseActivity(){
    override fun getLayoutID(): Int = R.layout.activity_setting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()
        initFragment()
    }

    private fun initActionBar() {
        setTitle(R.string.settingTitle)
        if (supportActionBar!=null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }
    private fun initFragment() {
        fragmentManager.beginTransaction().replace(android.R.id.content,PrefFragment()).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class PrefFragment : PreferenceFragment(){
        private var TAG = javaClass.simpleName

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = PrefHelper.NAME.PREF_CONF
            addPreferencesFromResource(R.xml.preference_setting)
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: Preference): Boolean {
            when(preference.key){
                PrefHelper.KEY.ALWAYS_ON_INFO -> { postEvent(SettingAlwaysOnInfoEvent((preference as CheckBoxPreference).isChecked)) }
                PrefHelper.KEY.ALWAYS_ON_PROGRESS -> { postEvent(SettingAlwaysOnProgressEvent((preference as CheckBoxPreference).isChecked)) }
                PrefHelper.KEY.GESTURE_BRIGHTNESS -> { postEvent(SettingGestureBrightnessEvent((preference as CheckBoxPreference).isChecked)) }
                PrefHelper.KEY.GESTURE_DOUBLE_TAP -> { postEvent(SettingGestureDoubleTapEvent((preference as CheckBoxPreference).isChecked)) }
                PrefHelper.KEY.GESTURE_PROGRESS -> { postEvent(SettingGestureProgressEvent((preference as CheckBoxPreference).isChecked)) }
                PrefHelper.KEY.GESTURE_VOLUME -> { postEvent(SettingGestureVolumeEvent((preference as CheckBoxPreference).isChecked)) }
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference)
        }

        fun postEvent(event:Any) = EventBus.getDefault().post(event)

        fun log(content:String) = Log.i(TAG,content)
    }
}