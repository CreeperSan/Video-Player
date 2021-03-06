package creepersan.videoplayer.Activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.LruCache
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import creepersan.videoplayer.Base.BaseActivity
import creepersan.videoplayer.Bean.FolderBean
import creepersan.videoplayer.Bean.VideoBean
import creepersan.videoplayer.Helper.*
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.util.*


class MainActivity : BaseActivity() {
    private val REQUEST_CODE = 1

    private var isHome = true;

    private lateinit var folderAdapter:FolderAdapter
    private lateinit var videoAdapter:VideoAdapter
    private lateinit var actionBar: ActionBar


    private val bitmapLruCache = object : LruCache<String,Bitmap>((Runtime.getRuntime().maxMemory()/4).toInt()){
        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: Bitmap?, newValue: Bitmap?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if(oldValue!=null)
                oldValue.recycle()
        }
    }

    override fun getLayoutID(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar();
        initPermission()
    }

    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_CODE)
        }else{
            initLater()
        }
    }
    private fun initLater(){
        initList()
        initData()
        initSwipeRefreshLayout()
    }
    private fun initActionBar(){
        if (supportActionBar != null){
            actionBar = supportActionBar as ActionBar
        }
    }
    private fun initList(){
        folderAdapter = FolderAdapter()
        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = folderAdapter
    }
    private fun initData(){
        val helper = ContentResolverHelper(contentResolver)
        helper.initData()
        val map = helper.getFoldersInfo()
        folderAdapter.setData(map)
    }
    private fun initSwipeRefreshLayout() {
        mainRefreshLayout.setOnRefreshListener {
            initList()
            initData()
            mainRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_main,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuOptionMainSetting -> {
                startActivity(SettingActivity::class.java)
            }
            R.id.menuOptionMainAbout -> {
                startActivity(AboutActivity::class.java)
            }
            R.id.menuOptionMainExit -> {
                postEvent(CommandHelper.COMMAND_EXIT)
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isHome){
            finish()
        }else{
            isHome = true;
            actionBar.setDisplayHomeAsUpEnabled(false)
            mainRecyclerView.adapter = folderAdapter
            setTitle(R.string.app_name)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.mainDialogRequestPermissionTitle)
                builder.setMessage(R.string.mainDialogRequestPermissionContent)
                builder.setPositiveButton(R.string.mainDialogRequestPermissionPositive) { p0, p1 -> initPermission() }
                builder.setNegativeButton(R.string.mainDialogRequestPermissionNegative,{ p0,p1 -> finish() })
                builder.setCancelable(false)
                builder.show()
            }else {
                initLater()
            }
        }
    }

    inner class FolderAdapter : RecyclerView.Adapter<FolderHolder>(){
        var folderList = ArrayList<FolderBean>()

        fun setData(folderMap:HashMap<String,FolderBean>){
            for (tmpFolder in folderMap.keys){
                val tempFolder = folderMap.get(tmpFolder)
                if (tempFolder!=null) folderList.add(tempFolder)
            }
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: FolderHolder, position: Int) {
            holder.title.text = folderList[position].folderName
            holder.content.text = "${folderList[position].videoList.size} ${getString(R.string.mainVideos)}"
            holder.itemView.setOnClickListener {
                isHome = false;
                title = folderList[position].folderName
                videoAdapter = VideoAdapter(folderList[position])
                mainRecyclerView.adapter = videoAdapter
                videoAdapter.notifyDataSetChanged()
                actionBar.setDisplayHomeAsUpEnabled(true)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FolderHolder
                = FolderHolder(layoutInflater.inflate(R.layout.item_main_folder,parent,false))

        override fun getItemCount(): Int = folderList.size

    }
    inner class VideoAdapter(var folderBean: FolderBean) : RecyclerView.Adapter<VideoHolder>(){

        fun setData(folderBean: FolderBean){
            this.folderBean = folderBean
            notifyDataSetChanged()
        }
        fun playVideo(videoBean: VideoBean){
            val intent = Intent(this@MainActivity,PlayActivity::class.java)
            IntentHelper.makePlayIntent(intent,videoBean.videoPath,videoBean.name,videoBean,folderBean)
            startActivity(intent)
        }
        fun makeOptionDialog(videoBean: VideoBean){

        }

        override fun onBindViewHolder(holder: VideoHolder, position: Int) {
            val videoBean = folderBean.videoList.get(position)
            holder.title.text = videoBean.name
            //持续时间
            var durationLong: Long
            try {
                durationLong = videoBean.duration.toLong()
            } catch (e: Exception) {
                durationLong = Long.MIN_VALUE
            }
            holder.durationText.text = TimeHelper.getDurationStr(durationLong,this@MainActivity)
            holder.info.text = "${videoBean.videoWidth}×${videoBean.videoHeight}"
            holder.createTime.text = "${NumberHelper.getFileSizeFromB_MB(videoBean.size)}MB"
            //获取图片
            holder.preview.setImageResource(R.drawable.ic_local_movies_black_24dp)
            holder.loadImage(videoBean.videoPath)
            holder.itemView.setOnClickListener {
                playVideo(videoBean)
            }
            holder.itemView.setOnLongClickListener(object : View.OnLongClickListener{
                override fun onLongClick(p0: View?): Boolean {
                    makeOptionDialog(videoBean)
                    return true
                }
            })
        }

        override fun getItemCount(): Int = folderBean.videoList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder =
                VideoHolder(layoutInflater.inflate(R.layout.item_main_video,parent,false))

    }
    inner class FolderHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        var icon:ImageView = itemView.findViewById<ImageView>(R.id.itemMainFolderIcon)
        var title:TextView = itemView.findViewById<TextView>(R.id.itemMainFolderTitle)
        var content:TextView = itemView.findViewById<TextView>(R.id.itemMainFolderContent)
    }
    inner class VideoHolder(itemView:View) :RecyclerView.ViewHolder(itemView){
        var durationText = itemView.findViewById<TextView>(R.id.itemMainVideoDuration)
        var preview = itemView.findViewById<ImageView>(R.id.itemMainVideoPreview)
        var title = itemView.findViewById<TextView>(R.id.itemMainVideoTitle)
        var info = itemView.findViewById<TextView>(R.id.itemMainVideoInfo)
        var createTime = itemView.findViewById<TextView>(R.id.itemMainVideoCreateTime)

        val imageLoadThread = ImageLoaderThread()

        fun loadImage(path:String){
            if (bitmapLruCache.get(path)!=null){
                preview.setImageBitmap(bitmapLruCache.get(path))
            }else{
                imageLoadThread.setPath(path)
            }
        }

        inner class ImageLoaderThread:Thread(){
            private var path:String = ""

            fun close(){
                interrupt()
            }
            fun setPath(path: String){
                this.path = path
                start()
            }

            override fun run() {
                super.run()
                val bitmap = ThumbnailUtils.createVideoThumbnail(path,MediaStore.Video.Thumbnails.MINI_KIND)
                runOnUiThread{
                    if (bitmap!=null){
                        bitmapLruCache.put(path,bitmap)
                        this@VideoHolder.preview.setImageBitmap(bitmap)
                    }else{
                        this@VideoHolder.preview.setImageResource(R.drawable.ic_error_outline_black_24dp)
                    }
                }
            }
        }
    }

}