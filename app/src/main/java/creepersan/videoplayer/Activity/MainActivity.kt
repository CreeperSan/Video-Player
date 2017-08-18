package creepersan.videoplayer.Activity

import android.content.Intent
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import creepersan.videoplayer.Base.BaseActivity
import creepersan.videoplayer.Bean.FolderBean
import creepersan.videoplayer.Bean.VideoBean
import creepersan.videoplayer.Helper.ContentResolverHelper
import creepersan.videoplayer.Helper.IntentHelper
import creepersan.videoplayer.Helper.TimeHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : BaseActivity() {
    private var isHome = true;

    private lateinit var folderAdapter:FolderAdapter
    private lateinit var videoAdapter:VideoAdapter
    private lateinit var actionBar: ActionBar

    override fun getLayoutID(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
        initActionBar();
        initList()
        initData()
        initSwipeRefreshLayout();
    }

    private fun initService() {
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
            R.id.menuOptionMainRefresh -> {

            }
            R.id.menuOptionMainAbout -> {

            }
            R.id.menuOptionMainExit -> {

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
            IntentHelper.makePlayIntent(intent,videoBean.videoPath,videoBean)
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
            holder.info.text = videoBean.folderName
            holder.createTime.text = videoBean.videoPath
            //获取图片
            holder.preview.setImageBitmap(ThumbnailUtils.createVideoThumbnail(videoBean.videoPath,MediaStore.Video.Thumbnails.MINI_KIND))
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
    }
}