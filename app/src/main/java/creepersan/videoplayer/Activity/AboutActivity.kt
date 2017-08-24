package creepersan.videoplayer.Activity

import android.os.Bundle
import android.view.MenuItem
import creepersan.videoplayer.Base.BaseActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity() {

    override fun getLayoutID(): Int = R.layout.activity_about

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.aboutTitle)

        try {
            aboutVersion.text = packageManager.getPackageInfo(packageName,0).versionName
        } catch (e: Exception) {
            aboutVersion.text = getString(R.string.aboutUnknownVersion)
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> { finish() }
        }
        return super.onOptionsItemSelected(item)
    }

}