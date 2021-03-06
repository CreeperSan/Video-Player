package creepersan.videoplayer.Activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.view.*
import android.widget.SeekBar
import creepersan.videoplayer.Base.BaseActivity
import creepersan.videoplayer.Bean.FolderBean
import creepersan.videoplayer.Event.*
import creepersan.videoplayer.Helper.*
import kotlinx.android.synthetic.main.activity_play.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


class PlayActivity : BaseActivity(), SurfaceHolder.Callback, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {
    object FLAG{
        val CURRENT_POS = "CurrentPos"
        val IS_FORCE_ROTATE = "isForceRotate"
        val IS_LANDSCAPE = "isLandscape"
    }

    private var videoPath = ""
    private var videoName = ""

    private var isAlwaysOnProgress = true           //设置的标志位
    private var isAlwaysOnInfo = true               //设置的标志位
    private var isDoubleTapGesture = true          //设置的标志位
    private var isProgressGesture = true            //设置的标志位
    private var isBrightnessGesture = true          //设置的标志位
    private var isVolumeGesture = true              //设置的标志位
    private var isLockScreen = false
    private var isShowControl = false
    private var isTapVideo = true
    private var isSeeking = false
    private var isTouchSeekingX = false
    private var isTouchSeekingYLeft = false
    private var isTouchSeekingYRight = false
    private var isCanPreview = true
    private var isExit = true
    private var isFastPreviewProgress = false
    private var isLandscape = false
    private var isForceRotate = false
    private var isPlayerReady = false
    private var isRegisterReceiver = false

    private var saveTouchPosX = 0f
    private var saveTouchPosY = 0f
    private val adjustThresholdXAxis = 40f
    private var adjustThresholdYAxis = 120f
    private var saveMediaPlayerTime = 0
    private var saveMediaPlayerState = false
    private var saveBrightnessLevel = -1
    private var saveVolumeLevel = 0
    private var saveCurrentPosition = -1
    private var newMediaPlayerTime = 0L
    private var volumeMax = 0
    private var brightnessMax = 255f
    private var newBrightness = 0
    private var retryCount = 0
    private val retryCountMax = 3
    private var saveTapTimeStamp = 0L
    private val doubleTapTimeSnap = 300
    private var videoSize = "0MB"

    private val spannableStrCenterSmall = RelativeSizeSpan(0.5f)
    private val timeRefreshThread = ProgressBarRefreshThread()
    private lateinit var surfaceHolder:SurfaceHolder
    private lateinit var mediaPlayer:MediaPlayer
    private lateinit var audioManager:AudioManager
    private var folderBean:FolderBean? = null
    private val batteryReceiver = BatteryInfoReceiver()

    override fun getLayoutID(): Int = R.layout.activity_play

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            outState.putInt(FLAG.CURRENT_POS,mediaPlayer.currentPosition)
        } catch (e: Exception) {
        }
        outState.putBoolean(FLAG.IS_LANDSCAPE,isLandscape)
        outState.putBoolean(FLAG.IS_FORCE_ROTATE,isForceRotate)
        isExit = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initKeepScreenAwake()           //保持屏幕常亮
        initIntent()
        initFlag(savedInstanceState)    //处理关键标志位
        initSetting()                   //处理设置的配置
        if (!initFileIsExist()){
            showDialogSimple(R.string.dialogPlayError,R.string.dialogVideoNotExist)
            return
        }
        initRotate()
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer.pause()
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
        } catch (e: Exception) {
        }
        timeRefreshThread.close()
        if (!isExit){
            isExit = true
        }
        if(isRegisterReceiver){
            isRegisterReceiver = false
            unregisterReceiver(batteryReceiver)
        }
    }
    override fun onStop() {
        super.onStop()
        try {
            pauseVideo()
        } catch (e: Exception) {
        }
    }

    private fun startInit(){
        hideSystemUI()
        initMediaPlayer()
        initAudioManager()
        initBattery()
        initSurfaceView()
        initButton()
        initToolBar()
        initProgressBar(mediaPlayer.currentPosition,mediaPlayer.duration)
        initAlwaysOn()
    }
    private fun initKeepScreenAwake() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    private fun initFileIsExist():Boolean = File(videoPath).exists()
    private fun initFlag(bundle: Bundle?) {
        if (bundle!=null){
            saveCurrentPosition = bundle.getInt(FLAG.CURRENT_POS,-1)
            isForceRotate = bundle.getBoolean(FLAG.IS_FORCE_ROTATE,false)
            isLandscape = bundle.getBoolean(FLAG.IS_LANDSCAPE,true)
        }
    }
    private fun initSetting() {
        isAlwaysOnInfo = PrefHelper.getAlwaysOnInfoStatus(this)
        isAlwaysOnProgress = PrefHelper.getAlwaysOnProgressStatus(this)
        isDoubleTapGesture = PrefHelper.getGestureDoubleTapStatus(this)
        isProgressGesture = PrefHelper.getGestureProgressStatus(this)
        isVolumeGesture = PrefHelper.getGestureVolumeStatus(this)
        isBrightnessGesture = PrefHelper.getGestureBrightnessStatus(this)
    }
    private fun initRotate() {
        if (isForceRotate){
            startInit()
            return
        }
        val tempPlayer = MediaPlayer()
        tempPlayer.reset()
        tempPlayer.setDataSource(videoPath)
        tempPlayer.prepare()
        var isPortrait = true
        var currentIsPortrait = true
        if (tempPlayer.videoWidth > tempPlayer.videoHeight){
            isPortrait = false
        }
        tempPlayer.reset()
        tempPlayer.release()
        if (resources.configuration.orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            currentIsPortrait = false
        }
        //判断方向
        if (isPortrait){
            if (currentIsPortrait){
                log("方向正确")
                startInit()
            }else{
                log("旋转到竖屏")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }else{
            if (currentIsPortrait){
                log("旋转到横屏")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }else{
                log("方向正确")
                startInit()
            }
        }
    }
    private fun initIntent() {
        val baseIntent = IntentHelper.parseIntent(intent)
        if (baseIntent is PlayIntentInfo){
            videoPath = baseIntent.filePath
            videoName = baseIntent.videoBean.name
            folderBean = baseIntent.folderBean
            videoSize = "${NumberHelper.getFileSizeFromB_MB(baseIntent.videoBean.size)}MB"
        }
    }
    private fun initAudioManager() {
       audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }
    private fun initBattery() {
        if (isAlwaysOnInfo){
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(batteryReceiver,intentFilter)
            isRegisterReceiver = true
        }
    }
    private fun initToolBar() {
        setSupportActionBar(playToolbar)
        setTitle(videoName)
        (supportActionBar as ActionBar).setDisplayHomeAsUpEnabled(true)
    }
    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(videoPath)
        mediaPlayer.prepare()
        isPlayerReady = true
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnInfoListener(this)
        mediaPlayer.setOnSeekCompleteListener(this)
        mediaPlayer.setOnVideoSizeChangedListener(this)
        if (saveCurrentPosition>0){
            mediaPlayer.seekTo(saveCurrentPosition)
        }
        timeRefreshThread.start()
    }
    private fun initScale() {
        val videoHeight = mediaPlayer.videoHeight.toFloat()
        val videoWidth = mediaPlayer.videoWidth.toFloat()
        val screenHeight = playTouchZone.height.toFloat()
        val screenWidth = playTouchZone.width.toFloat()
        val layoutParams = playSurfaceView.layoutParams
        val isScreenVertical = screenHeight>screenWidth
        val isVideoVertical = videoHeight>videoWidth
        val screenTimes = screenWidth / screenHeight
        val videoTimes = videoWidth / videoHeight
        if (isScreenVertical){
            if (isVideoVertical){
                if (screenTimes<videoTimes){
                    layoutParams.width = screenWidth.toInt()
                    layoutParams.height = (screenWidth / ( videoWidth / videoHeight )).toInt()
                }else{
                    layoutParams.height = screenHeight.toInt()
                    layoutParams.width = (screenHeight/(videoHeight/videoWidth)).toInt()
                }
            }else{
                layoutParams.width = screenWidth.toInt()
                layoutParams.height = (screenWidth/(videoWidth/videoHeight)).toInt()
            }
        }else{
            if (isVideoVertical){
                layoutParams.height = screenHeight.toInt()
                layoutParams.width = (screenHeight/(videoHeight/videoWidth)).toInt()
            }else{
                if (screenTimes>videoTimes){
                    layoutParams.height = screenHeight.toInt()
                    layoutParams.width = (screenHeight/(videoHeight/videoWidth)).toInt()
                }else{
                    layoutParams.width = screenWidth.toInt()
                    layoutParams.height = (screenWidth*(videoHeight/videoWidth)).toInt()
                }
            }
        }
        playSurfaceView.invalidate()
//        log("video:$videoWidth*$videoHeight  screen:$screenWidth*$screenHeight  view:${layoutParams.width}*${layoutParams.height}")
    }
    private fun initSurfaceView() {
        surfaceHolder = playSurfaceView.holder
        surfaceHolder.addCallback(this)
        playTouchZone.setOnTouchListener(this)
        val onGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                if (isPlayerReady){
                    initScale()
                    playSurfaceView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        }
        playSurfaceView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }
    private fun initButton() {
        playButtonPlay.setOnClickListener{ playOrResume() }
        playButtonPrevious.setOnClickListener { playPrevious() }
        playButtonNext.setOnClickListener { playNext() }
        playButtonRotate.setOnClickListener { playRotateScreen() }
        playButtonLock.setOnClickListener { playLockScreen() }
        playUnlockZone.setOnClickListener {//解锁按下了
            isLockScreen = false
            showUnlockZoneVisibility(View.GONE)
            isShowControl = true
            showPlayerControlUIVisibility(View.VISIBLE)
        }
    }
    private fun initProgressBar(current: Int,total:Int) {
        playProgressCurrent.text = TimeHelper.getDurationStr(mediaPlayer.currentPosition.toLong(),this)
        playProgressTotal.text = TimeHelper.getDurationStr(mediaPlayer.duration.toLong(),this)
        playProgressSeekBar.max = mediaPlayer.duration
        playProgressSeekBar.progress = mediaPlayer.currentPosition
        playProgressSeekBar.setOnSeekBarChangeListener(this)
    }
    private fun initAlwaysOn() {
        if (isAlwaysOnInfo){
            showAlwaysOnInfoVisibility(View.VISIBLE)
        }else{
            showAlwaysOnInfoVisibility(View.GONE)
        }
        if (isAlwaysOnProgress){
            showAlwaysOnProgressVisibility(View.VISIBLE)
        }else{
            showAlwaysOnProgressVisibility(View.GONE)
        }
    }

    private fun refreshPlayerInfo(){
        //工具栏初始化
        setTitle(videoName)
        //进度条初始化
        playProgressSeekBar.max = mediaPlayer.duration
        playProgressTotal.text = TimeHelper.getDurationStr(mediaPlayer.duration.toLong(),this)
//        refreshIntent()
    }
    private fun refreshIntent(){
        //修改Intent的内容
        intent.putExtra(IntentKey.FILE_PATH,videoPath)
        intent.putExtra(IntentKey.FILE_NAME,videoName)
    }
    private fun isRotateCorrect():Boolean{
        if (isForceRotate){
            startInit()
            return true
        }
        val tempPlayer = MediaPlayer()
        tempPlayer.reset()
        tempPlayer.setDataSource(videoPath)
        tempPlayer.prepare()
        var isPortrait = true
        var currentIsPortrait = true
        if (tempPlayer.videoWidth > tempPlayer.videoHeight){
            isPortrait = false
        }
        tempPlayer.reset()
        tempPlayer.release()
        if (resources.configuration.orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            currentIsPortrait = false
        }
        //判断方向
        return if (isPortrait){
            currentIsPortrait
        }else{
            !currentIsPortrait
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_play,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(isPlayerReady){
            pauseVideo()
        }
        when(item.itemId){
            android.R.id.home -> { finish() }
            R.id.menuOptionPlaySetting -> { startActivity(SettingActivity::class.java) }
            R.id.menuOptionPlayVideoInfo -> { makeVideoInfoDialog() }
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
        playToolbar.visibility = visible
        playButtonZone.visibility = visible
        playProgressZone.visibility = visible

    }
    private fun showCenterTextVisibility(visible: Int){
        playCenterZone.visibility = visible
    }
    private fun showCenterText(content:CharSequence){
        playCenterText.text = content
    }
    private fun showVerticalControlUIVisibility(visible: Int){
        playVerticalSlideInfoUI.visibility = visible
    }
    private fun showVerticalControlIcon(resID:Int){
        playVerticalSlideInfoIcon.setImageResource(resID)
    }
    private fun showVerticalControlUIValue(value:Int){
        playVerticalSlideInfoProgressBar.setProgress(value)
    }
    private fun showSystemUI(){
        playSurfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }
    private fun hideSystemUI(){
        playTouchZone.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        playTouchZone.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }
    private fun showDialogSimple(titleResId:Int,contentResId:Int){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(titleResId))
        builder.setMessage(getString(contentResId))
        builder.setPositiveButton(R.string.playDialogDefaultPositiveText,null)
        builder.show()
    }
    private fun makeVideoInfoDialog(){
        if (!File(videoPath).exists()) return
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(videoName)
        if (isPlayerReady){
            builder.setMessage("${getString(R.string.playDialogVideoName)} : ${videoName}\n" +
                    "${getString(R.string.playDialogVideoDuration)} : ${TimeHelper.getDurationStr(mediaPlayer.duration.toLong(),this)}\n" +
                    "${getString(R.string.playDialogVideoWidth)} : ${mediaPlayer.videoWidth}\n" +
                    "${getString(R.string.playDialogVideoHeight)} : ${mediaPlayer.videoHeight}\n" +
                    "${getString(R.string.playDialogVideoSize)} : ${videoSize}\n" +
                    "${getString(R.string.playDialogVideoPath)} : ${videoPath}")
        }else{
            builder.setMessage(R.string.playDialogPlayerNotReady)
        }
        builder.show()
    }

    /**
     *      System
     *      * 亮度的取值在0-255
     */
    private fun getScreenBrightness(defaultBrightness:Int):Int =
            Settings.System.getInt(contentResolver,Settings.System.SCREEN_BRIGHTNESS,defaultBrightness)
    private fun getScreenBrightness():Int =
            getScreenBrightness(125)
    private fun setScreenBrightness(brightness:Int){
        val lp = window.attributes
        lp.screenBrightness = brightness / brightnessMax
        window.attributes = lp
    }
    private fun getVolume():Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    private fun setVolume(volume:Int){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume,0)
    }

    /**
     *      Media Player
     */

    private fun playOrResume(){
        if (mediaPlayer.isPlaying){
            pauseVideo()
        }else{
            playVideo()
        }
    }
    private fun playVideo(){
        if (!isPlayerReady) return
        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
        }
        playButtonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp)
    }
    private fun pauseVideo(){
        if(mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
        playButtonPlay.setImageResource(R.drawable.ic_pause_black_24dp)
    }
    private fun replayVideo(){
        isPlayerReady = false
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(videoPath)
        mediaPlayer.prepare()
        mediaPlayer.start()
        isPlayerReady = true
    }
    private fun playPrevious(){
        if (folderBean==null){
            replayVideo()
        }else{
            val videoList = folderBean!!.videoList
            val listSize = videoList.size
            if (listSize==1)replayVideo()
            var currentPos = -100
            for (i in 0..videoList.size){
                if (videoList[i].videoPath == videoPath){
                    currentPos = i
                    break
                }
            }
            currentPos--
            if ((currentPos>=-1) and  (currentPos<listSize-1)){
                if (currentPos==-1){
                    currentPos = listSize-1
                }
                videoPath = videoList[currentPos].videoPath
                videoName = videoList[currentPos].name
                refreshIntent()
                if(!isRotateCorrect()){
                    initRotate()
                }else{
                    videoPath = videoList[currentPos].videoPath
                    videoName = videoList[currentPos].name
                    isPlayerReady = false
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(videoPath)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    isPlayerReady = true
                    refreshPlayerInfo()
                    initScale()
                }
            }else{
                replayVideo()
            }
        }
    }
    private fun playNext(){
        if (!isPlayerReady) return
        if (folderBean==null){
            replayVideo()
        }else{
            val videoList = folderBean!!.videoList
            val listSize = videoList.size
            if (listSize==1)replayVideo()
            var currentPos = -1
            for (i in 0..videoList.size){
                if (videoList[i].videoPath == videoPath){
                    currentPos = i
                    break
                }
            }
            currentPos++
            if ((currentPos>0) and  (currentPos<=listSize)){
                if (currentPos==listSize){
                    currentPos = 0
                }
                videoPath = videoList[currentPos].videoPath
                videoName = videoList[currentPos].name
                refreshIntent()
                if (!isRotateCorrect()){
                    initRotate()
                }else{
                    isPlayerReady = false
                    if (mediaPlayer.isPlaying){
                        mediaPlayer.pause()
                    }
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(videoPath)
                    mediaPlayer.prepare()
                    isPlayerReady = true
                    mediaPlayer.start()
                    refreshPlayerInfo()
                    initScale()
                }
            }else{
                replayVideo()
            }
        }
    }
    private fun playLockScreen(){
        isLockScreen = true
        showPlayerControlUIVisibility(View.GONE)
        showUnlockZoneVisibility(View.VISIBLE)
    }
    private fun playRotateScreen(){
        isForceRotate = true
        if (isLandscape){
            isLandscape = false
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }else{
            isLandscape = true
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    override fun onVideoSizeChanged(p0: MediaPlayer?, p1: Int, p2: Int) {
//        log("onVideoSizeChange")
    }
    override fun onSeekComplete(p0: MediaPlayer?) {
//        log("OnSeekComplete")
    }
    override fun onPrepared(p0: MediaPlayer?) {
//        log("onPrepared")
    }
    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
//        log("onInfo")
        return false
    }
    override fun onError(player: MediaPlayer, what: Int, extra: Int): Boolean {
        logE("发生onError错误！！  what:$what  extra:$extra")
        if (retryCount<retryCountMax){
            logE("错误重试")
            isPlayerReady = false
            mediaPlayer.reset()
            mediaPlayer.setDataSource(videoPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlayerReady = true
            retryCount++
            return true
        }else{
            logE("错误重试失败！！！！！！！！")
            if ((folderBean!=null)and(folderBean!!.videoList.size!=0) ){
                toast(R.string.playErrorAndPlayNext)
                return false
            }else{
                toast(R.string.playError)
                isPlayerReady = false
                mediaPlayer.stop()
                return true
            }
        }
    }
    override fun onCompletion(player: MediaPlayer) {
        if (mediaPlayer.duration>1000){
            playNext()
        }
        retryCount = 0
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
        mediaPlayer.seekTo(p0.progress-1)
        if (saveMediaPlayerState){
            playVideo()
        }else{
            pauseVideo()
        }
    }

    /**
     *      SurfaceView
     */
    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
//        mediaPlayer.setDisplay(p0)
    }
    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        try {
            mediaPlayer.setDisplay(null)
        } catch (e: Exception) {
        }
    }
    override fun surfaceCreated(p0: SurfaceHolder) {
        log("SurfaceView准备好了")
        mediaPlayer.setDisplay(p0)
    }
    override fun onTouch(p0: View?, motionEvent: MotionEvent): Boolean {
        when(motionEvent.action){
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{    //如果是松开
                hideSystemUI()
                if (isTapVideo){    //如果是轻触视频
                    isShowControl = !isShowControl
                    if (isLockScreen){//如果已经锁屏了
                        if (isShowControl){
                            showUnlockZoneVisibility(View.VISIBLE)
                        }else{
                            showUnlockZoneVisibility(View.GONE)
                        }
                    }else{//如果没锁屏
                        if (isDoubleTapGesture and isPlayerReady){
                            val currentTimeStamp = System.currentTimeMillis()
                            if (currentTimeStamp - saveTapTimeStamp < doubleTapTimeSnap){
                                playOrResume()
                            }
                            saveTapTimeStamp = currentTimeStamp
                        }
                        if (isShowControl){
                            showPlayerControlUIVisibility(View.VISIBLE)
                        }else{
                            showPlayerControlUIVisibility(View.GONE)
                        }
                    }
                }else{      //如果还有其他操作
                    if(isLockScreen)return true
                    if (isTouchSeekingX){//如果是X轴上滑动了
                        isTouchSeekingX = false
                        showCenterTextVisibility(View.GONE)
                        mediaPlayer.seekTo((newMediaPlayerTime-1).toInt())
                        newMediaPlayerTime = 0
                        if (saveMediaPlayerState){
                            playVideo()
                        }else{
                            pauseVideo()
                        }
                    }
                    if (isTouchSeekingYLeft){
                        showVerticalControlUIVisibility(View.GONE)
                        isTouchSeekingYLeft = false
                        saveBrightnessLevel = newBrightness
                    }
                    if(isTouchSeekingYRight){
                        showVerticalControlUIVisibility(View.GONE)
                        isTouchSeekingYRight = false
                    }
                    //X轴的快速进度预览
                    if (isFastPreviewProgress){
                        isFastPreviewProgress = false
                        playProgressZone.visibility = View.GONE
                    }
                }
                //必须初始化的
                isTapVideo = true
            }
            MotionEvent.ACTION_MOVE->{  //如果是移动
                if(isLockScreen)return true
                var currentX = motionEvent.x
                val currentY = motionEvent.y
                //判断是否为移动
                if (!isProgressGesture){
                    currentX = saveTouchPosX
                }//为适配进度手势设置
                if (!(isTouchSeekingX or isTouchSeekingYLeft or isTouchSeekingYRight)){//还没确定是移动
                    if (Math.abs(currentX - saveTouchPosX) > adjustThresholdXAxis){
                        isTapVideo = false
                        isTouchSeekingX = true
                        if (!isShowControl){
                            playProgressZone.visibility = View.VISIBLE
                            isFastPreviewProgress = true
                        }
                    }else if (Math.abs(currentY - saveTouchPosY) > adjustThresholdXAxis){
                        isTapVideo = false
                        if ( ((currentX < playTouchZone.width/2) or !isVolumeGesture ) and isBrightnessGesture ){
                            showVerticalControlUIVisibility(View.VISIBLE)
                            showVerticalControlIcon(R.drawable.ic_brightness_high_black_24dp)
                            playVerticalSlideInfoProgressBar.setProgressMax(brightnessMax.toInt())
                            isTouchSeekingYLeft = true
                            if (saveBrightnessLevel<0){
                                saveBrightnessLevel = getScreenBrightness()
                            }
                        }else if ( ((currentX >= playTouchZone.width/2) or !isBrightnessGesture ) and isVolumeGesture ){
                            showVerticalControlUIVisibility(View.VISIBLE)
                            showVerticalControlIcon(R.drawable.ic_volume_up_black_24dp)
                            playVerticalSlideInfoProgressBar.setProgressMax(volumeMax)
                            isTouchSeekingYRight = true
                            saveVolumeLevel = getVolume()
                        }
                    }
                }else{//的确是移动
                    val screenHeight = playTouchZone.height.toFloat()
                    if (isTouchSeekingX){//是X轴的移动的话
                        val screenWidth = playTouchZone.width.toFloat()
                        val currentXPercent = (currentX - saveTouchPosX)/screenWidth
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
                            mediaPlayer.seekTo((newMediaPlayerTime-1).toInt())
                        }
                    }else{
                        val isRaise = !(currentY > saveTouchPosY)
                        if (isTouchSeekingYLeft){
                            adjustThresholdYAxis = screenHeight / brightnessMax
                            val times = (Math.abs(saveTouchPosY - currentY)/adjustThresholdYAxis).toInt()
                            if (isRaise){
                                newBrightness = saveBrightnessLevel + times
                            }else{
                                newBrightness = saveBrightnessLevel - times
                            }
                            if (newBrightness<0){
                                newBrightness = 0
                            }else if(newBrightness > brightnessMax){
                                newBrightness = brightnessMax.toInt()
                            }
                            setScreenBrightness(newBrightness)
                            showVerticalControlUIValue(newBrightness)
                        }
                        if (isTouchSeekingYRight){
                            adjustThresholdYAxis = screenHeight / volumeMax
                            val times = (Math.abs(saveTouchPosY - currentY)/adjustThresholdYAxis).toInt()
                            if(isRaise){
                                setVolume(saveVolumeLevel + times)
                            }else{
                                setVolume(saveVolumeLevel - times)
                            }
                            showVerticalControlUIValue(getVolume())
                        }
                    }
                }
            }
            MotionEvent.ACTION_DOWN->{  //如果是按下
                if(isLockScreen)return true
                saveTouchPosX = motionEvent.x
                saveTouchPosY = motionEvent.y
                saveMediaPlayerTime = mediaPlayer.currentPosition
                saveMediaPlayerState = mediaPlayer.isPlaying
            }
        }
        return true
    }

    /**
     *      EventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)//内部进度检测 Event
    fun onViewProgressEvent(event: MediaProgressResultEvent) {
        if(!isPlayerReady){
            return
        }
        if (isSeeking){
            playProgressCurrent.text = TimeHelper.getDurationStr(playProgressSeekBar.progress.toLong(),this)
            mediaPlayer.seekTo(playProgressSeekBar.progress-1)
        }else{
            playProgressCurrent.text = TimeHelper.getDurationStr(event.current.toLong(),this)
            playProgressSeekBar.progress = event.current
        }
        if (isAlwaysOnProgress){
            if (isPlayerReady){
                playAlwaysOnProgress.text = "${TimeHelper.getDurationStr(mediaPlayer.currentPosition.toLong(),this)} / ${TimeHelper.getDurationStr(mediaPlayer.duration.toLong(),this)}"
            }else{
                playAlwaysOnProgress.text = getString(R.string.playAlwaysOnProgressDefaultText)
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingAlwaysOnInfoEvent(event:SettingAlwaysOnInfoEvent){
        if (event.newStatus){
            if (!isRegisterReceiver){
                val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                registerReceiver(batteryReceiver,intentFilter)
                isRegisterReceiver = true
                showAlwaysOnInfoVisibility(View.VISIBLE)
            }
        }else{
            if (isRegisterReceiver){
                if (isRegisterReceiver){
                    isRegisterReceiver = false
                    unregisterReceiver(batteryReceiver)
                    showAlwaysOnInfoVisibility(View.GONE)
                }
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingAlwaysOnProgressEvent(event:SettingAlwaysOnProgressEvent){
        if (event.newStatus){
            showAlwaysOnInfoVisibility(View.VISIBLE)
        }else{
            showAlwaysOnProgressVisibility(View.GONE)
        }
        isAlwaysOnProgress = event.newStatus
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingGestureDoubleTapEvent(event:SettingGestureDoubleTapEvent){
        isDoubleTapGesture = event.newStatus
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingGestureProgressEvent(event:SettingGestureProgressEvent){
        isProgressGesture = event.newStatus
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingGestureVolumeEvent(event:SettingGestureVolumeEvent){
        isVolumeGesture = event.newStatus
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingGestureBrightnessEvent(event:SettingGestureBrightnessEvent){
        isBrightnessGesture = event.newStatus
    }


    /**
     *      内部类
     */
    inner class ProgressBarRefreshThread : Thread(){
        private var isRunning = true
        var refreshSpan = 300L

        fun close(){
            isRunning = false
        }

        override fun run() {
            super.run()
            while (isRunning){
                postEvent(MediaProgressResultEvent(mediaPlayer.currentPosition))
                isCanPreview = true
                try {
                    Thread.sleep(refreshSpan)
                } catch (e: Exception) {
                    close()
                }
            }
        }
    }
    inner class BatteryInfoReceiver : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent) {
            val batteryLevel = intent.getIntExtra("level",0)
            val batteryStatus = intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_UNKNOWN)
            val timeStr = TimeHelper.getTimeStr(System.currentTimeMillis(),this@PlayActivity)
            val spannableString = SpannableString("${timeStr}  ${batteryLevel}%  ")
            var batteryIconID = R.drawable.ic_battery_charging_full_black_24dp
            if (batteryLevel < 20){
                batteryIconID = R.drawable.ic_battery_alert_white_14dp
            }else if (batteryLevel < 30){
                batteryIconID = R.drawable.ic_battery_20_white_14dp
            }else if (batteryLevel < 50){
                batteryIconID = R.drawable.ic_battery_30_white_14dp
            }else if (batteryLevel < 60){
                batteryIconID = R.drawable.ic_battery_50_white_14dp
            }else if (batteryLevel < 80){
                batteryIconID = R.drawable.ic_battery_60_white_14dp
            }else if (batteryLevel < 90){
                batteryIconID = R.drawable.ic_battery_80_white_14dp
            }else if (batteryLevel < 100){
                batteryIconID = R.drawable.ic_battery_90_white_14dp
            }else if ((batteryStatus!=BatteryManager.BATTERY_STATUS_FULL) and (batteryLevel==100)){
                batteryIconID = R.drawable.ic_battery_full_white_14dp
            }else if (batteryStatus == BatteryManager.BATTERY_STATUS_UNKNOWN){
                batteryIconID = R.drawable.ic_battery_unknown_white_14dp
            }
            val imageSpan = ImageSpan(this@PlayActivity,batteryIconID)
            spannableString.setSpan(imageSpan,spannableString.length-1,spannableString.length,Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            playAlwaysOnInfo.text = spannableString
        }
    }
}