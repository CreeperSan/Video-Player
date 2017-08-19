package creepersan.videoplayer.Activity

import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.*
import android.widget.SeekBar
import creepersan.videoplayer.Base.BaseActivity
import creepersan.videoplayer.Event.VideoProgressEvent
import creepersan.videoplayer.Helper.IntentHelper
import creepersan.videoplayer.Helper.PlayIntentInfo
import creepersan.videoplayer.Helper.TimeHelper
import kotlinx.android.synthetic.main.activity_play.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class PlayActivity : BaseActivity(), SurfaceHolder.Callback, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    private var videoPath = ""
    private var videoName = ""

    private var isMediaPlayerReady = false
    private var isAlwaysOnProgress = false
    private var isAlwaysOnInfo = false
    private var isLockScreen = false
    private var isShowControl = false
    private var isTapVideo = true
    private var isSeeking = false
    private var isSeekingX = false
    private var isTouchSeekingX = false
    private var isCanPreview = true

    private var touchPosX = 0f
    private var touchPosY = 0f
    private val touchMoveMax = 40f
    private var saveMediaPlayerTime = 0
    private var saveMediaPlayerState = false
    private var newMediaPlayerTime = 0L

    private val spannableStrCenterSmall = RelativeSizeSpan(0.5f)
    private val timeRefreshThread = ProgressBarRefreshThread()
    private lateinit var surfaceHolder:SurfaceHolder
    private val mediaPlayer = MediaPlayer()

    override fun getLayoutID(): Int = R.layout.activity_play

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent()
        initToolBar()
        initSurfaceView()
        initMediaPlayer()
        initButton()
        initProgressBar(mediaPlayer.currentPosition,mediaPlayer.duration)
    }

    override fun onResume() {
        super.onResume()
        surfaceHolder = playSeufaceView.holder
    }
    override fun onDestroy() {
        super.onDestroy()
        timeRefreshThread.close()
    }
    override fun onStop() {
        super.onStop()
        pauseVideo()
    }


    private fun initIntent() {
        val baseIntent = IntentHelper.parseIntent(intent)
        if (baseIntent is PlayIntentInfo){
            videoPath = baseIntent.filePath
            videoName = baseIntent.videoBean.name
        }
    }
    private fun initToolBar() {
        setSupportActionBar(playToolbar)
        setTitle(videoName)
        (supportActionBar as ActionBar).setDisplayHomeAsUpEnabled(true)
    }
    private fun initMediaPlayer() {
        if (isMediaPlayerReady){
            log("载入")
        }else{
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnErrorListener(this)
            mediaPlayer.setOnInfoListener(this)
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnSeekCompleteListener(this)
            mediaPlayer.setOnVideoSizeChangedListener(this)
            mediaPlayer.setDataSource(videoPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            timeRefreshThread.start()//开启线程
        }
    }
    private fun initSurfaceView() {
        surfaceHolder = playSeufaceView.holder
        surfaceHolder.addCallback(this)
        playSeufaceView.setOnTouchListener(this)
    }
    private fun initButton() {
        playButtonPlay.setOnClickListener{ playOrResume() }
        playButtonPrevious.setOnClickListener { playPrevious() }
        playButtonNext.setOnClickListener { playNext() }
        playButtonFullscreen.setOnClickListener { playFullscreen() }
        playButtonLock.setOnClickListener { playLockScreen() }
        playUnlockZone.setOnClickListener {//解锁按下了
            isLockScreen = false
            showUnlockZoneVisibility(View.GONE)
            isShowControl = true
            showPlayerControlUIVisibility(View.VISIBLE)
        }
    }
    private fun initProgressBar(current: Int,total:Int) {
        playProgressCurrent.text = TimeHelper.getDurationStr(current.toLong(),this)
        playProgressTotal.text = TimeHelper.getDurationStr(total.toLong(),this)
        playProgressSeekBar.max = total
        playProgressSeekBar.progress = current
        playProgressSeekBar.setOnSeekBarChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_play,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     *      UI
     */
    private fun showAlwaysOnUIVisibility(visible:Int){
        playAlwaysOnZone.visibility = visible
    }
    private fun showAlwaysOnProgressVisibility(visible: Int){
        playAlwaysOnProgress.visibility = visible
    }
    private fun showAlwaysOnInfoVisibility(visible: Int){
        playAlwaysOnInfo.visibility = visible
    }
    private fun showUnlockZoneVisibility(visible: Int){
        playUnlockZone.visibility = visible
    }
    private fun showPlayerControlUIVisibility(visible: Int){
        playUI.visibility = visible
    }
    private fun showVideoProgress(current:Int){
        playProgressCurrent.text = TimeHelper.getDurationStr(current.toLong(),this)
        playProgressSeekBar.progress = current
    }
    private fun showCenterTextVisibility(visible: Int){
        playCenterZone.visibility = visible
    }
    private fun showCenterText(content:CharSequence){
        playCenterText.text = content
    }
    private fun showSystemUI(){
//        playProgressCurrent.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        
    }
    private fun hideSystemUI(){
//        playProgressCurrent.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    /**
     *      Media Player
     */
    private fun playVideo(filePath:String){
        val videoFile = File(filePath)
        //检查文件是否存在
        if (!videoFile.exists()){
            toast(getString(R.string.playVideoNotExist))
            return
        }
    }
    private fun playOrResume(){
        if (mediaPlayer.isPlaying){
            mediaPlayer.pause()
            playButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp)
        }else{
            mediaPlayer.start()
            playButtonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp)
        }
    }
    private fun playVideo(){
        if(!mediaPlayer.isPlaying)
            mediaPlayer.start()
        playButtonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp)
    }
    private fun pauseVideo(){
        if(mediaPlayer.isPlaying)
            mediaPlayer.pause()
        playButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp)
    }
    private fun playPrevious(){}
    private fun playNext(){}
    private fun playLockScreen(){
        isLockScreen = true
        showPlayerControlUIVisibility(View.GONE)
        showUnlockZoneVisibility(View.VISIBLE)
    }
    private fun playFullscreen(){}

    private fun refreshPlayButton(){
        if (mediaPlayer.isPlaying){
            playButtonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp)
        }else{
            playButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp)
        }
    }

    override fun onVideoSizeChanged(p0: MediaPlayer?, p1: Int, p2: Int) {

    }
    override fun onSeekComplete(p0: MediaPlayer?) {

    }
    override fun onPrepared(p0: MediaPlayer?) {

    }
    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }
    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }
    override fun onCompletion(player: MediaPlayer) {
        playButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp)
    }

    /**
     *      SeekBar
     */
    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
    override fun onStartTrackingTouch(p0: SeekBar?) {
        isSeeking = true
        saveMediaPlayerState = mediaPlayer.isPlaying
        if(mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
        playButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp)
    }
    override fun onStopTrackingTouch(p0: SeekBar) {
        isSeeking = false
        mediaPlayer.seekTo(p0.progress)
        if (saveMediaPlayerState){
            playVideo()
        }else{
            pauseVideo()
        }
    }

    /**
     *      SurfaceView
     */
    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        mediaPlayer.setDisplay(surfaceHolder)
    }
    override fun surfaceDestroyed(p0: SurfaceHolder?) {

    }
    override fun surfaceCreated(p0: SurfaceHolder?) {
        mediaPlayer.setDisplay(surfaceHolder)
    }
    override fun onTouch(p0: View?, motionEvent: MotionEvent): Boolean {
        when(motionEvent.action){
            MotionEvent.ACTION_UP->{    //如果是松开
                if (isTapVideo){    //如果是轻触视频
                    isShowControl = !isShowControl
                    if (isLockScreen){//如果已经锁屏了
                        hideSystemUI()
                        if (isShowControl){
                            showUnlockZoneVisibility(View.VISIBLE)
                        }else{
                            showUnlockZoneVisibility(View.GONE)
                        }
                    }else{//如果没锁屏
                        if (isShowControl){
                            hideSystemUI()
                            showPlayerControlUIVisibility(View.VISIBLE)
                        }else{
                            hideSystemUI()
                            showPlayerControlUIVisibility(View.GONE)
                        }
                    }
                }else{      //如果还有其他操作
                    if(isLockScreen)return true
                    if (isTouchSeekingX){//如果是X轴上滑动了
                        isTouchSeekingX = false
                        showCenterTextVisibility(View.GONE)
                        mediaPlayer.seekTo(newMediaPlayerTime.toInt())
                        newMediaPlayerTime = 0
                        if (saveMediaPlayerState){
                            playVideo()
                        }else{
                            pauseVideo()
                        }
                    }
                }
                //必须初始化的
                isTapVideo = true
            }
            MotionEvent.ACTION_MOVE->{  //如果是移动
                if(isLockScreen)return true
                val currentX = motionEvent.x
                val currentY = motionEvent.y
                //判断是否为移动
                if (!isTouchSeekingX){//还没确定是移动
                    if (Math.abs(currentX - touchPosX) > touchMoveMax){
                        isTapVideo = false
                        isTouchSeekingX = true
                    }
                }else{//的确是移动
                    val screenHeight = playSeufaceView.height.toFloat()
                    if (isTouchSeekingX){//是X轴的移动的话
                        val screenWidth = playSeufaceView.width.toFloat()
                        val currentXPercent = (currentX - touchPosX)/screenWidth
                        if(mediaPlayer.isPlaying){
                            mediaPlayer.pause()
                            playButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp)
                        }
                        showCenterTextVisibility(View.VISIBLE)
                        newMediaPlayerTime = saveMediaPlayerTime+(currentXPercent*mediaPlayer.duration).toLong()
                        var newDValue = (currentXPercent*mediaPlayer.duration).toLong()
                        if(newMediaPlayerTime>mediaPlayer.duration){
                            newMediaPlayerTime = mediaPlayer.duration.toLong()
                            newDValue = (mediaPlayer.duration - saveMediaPlayerTime).toLong()
                        }else if(newMediaPlayerTime < 0){
                            newMediaPlayerTime = 0
                            newDValue = -saveMediaPlayerTime.toLong()
                        }
                        val spanStr = SpannableString("${TimeHelper.getDurationStr(newMediaPlayerTime,this)}\n${TimeHelper.getDurationStr(newDValue,this)}")
                        spanStr.setSpan(spannableStrCenterSmall,5,spanStr.length,Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        showCenterText(spanStr)
                        //进度条
                        playProgressSeekBar.progress = newMediaPlayerTime.toInt()
                        playProgressCurrent.text = TimeHelper.getDurationStr(newMediaPlayerTime,this)
                        if (isCanPreview){
                            isCanPreview = false
                            mediaPlayer.seekTo(newMediaPlayerTime.toInt())
                        }
                    }


                }
            }
            MotionEvent.ACTION_DOWN->{  //如果是按下
                if(isLockScreen)return true
                touchPosX = motionEvent.x
                touchPosY = motionEvent.y
                saveMediaPlayerTime = mediaPlayer.currentPosition
                saveMediaPlayerState = mediaPlayer.isPlaying
            }
        }
        return true
    }

    /**
     *      EventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onViewProgressEvent(event: VideoProgressEvent) {
        if(isTouchSeekingX){

        }else if (isSeeking){
            playProgressCurrent.text = TimeHelper.getDurationStr(playProgressSeekBar.progress.toLong(),this)
            mediaPlayer.seekTo(playProgressSeekBar.progress)
        }else{
            playProgressCurrent.text = TimeHelper.getDurationStr(event.current.toLong(),this)
            playProgressSeekBar.progress = event.current
        }
    }

    /**
     *      内部类
     */
    inner class ProgressBarRefreshThread : Thread(){
        private var isRunning = true
        var refreshSpan = 300L

        fun close(){
            isRunning = false
//            interrupt()
        }

        override fun run() {
            super.run()
            while (isRunning){
                postEvent(VideoProgressEvent(mediaPlayer.currentPosition,mediaPlayer.duration))
                isCanPreview = true
                try {
                    Thread.sleep(refreshSpan)
                } catch (e: Exception) {
                    close()
                }
            }
        }
    }
}