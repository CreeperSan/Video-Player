package creepersan.videoplayer.Helper

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import creepersan.videoplayer.Bean.FolderBean
import creepersan.videoplayer.Bean.VideoBean
import java.lang.Exception

class ContentResolverHelper(private val contentResolver: ContentResolver){
    private var folderMap = HashMap<String,FolderBean>()

    fun initData(){
        val folderNameSet = HashSet<String>()
        val cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,
                null,null,null)
        while (cursor.moveToNext()){
            val tempVideoBean = VideoBean(cursor)
            if (folderNameSet.contains(tempVideoBean.folderName)){//文件夹已存在
                val folderBean = folderMap.get(tempVideoBean.folderName)
                if (folderBean==null) throw Exception("初始化储存数据的视频文件夹为空！")
                folderBean.videoList.add(0,tempVideoBean)
            }else{//如果文件夹不存在
                folderNameSet.add(tempVideoBean.folderName)
                val folderBean = FolderBean(tempVideoBean.folderName,"")
                folderBean.videoList.add(tempVideoBean)
                folderMap.put(tempVideoBean.folderName,folderBean)
            }
        }
        cursor.close()
    }


    protected fun log(content:String) = Log.i("Helper",content)

    fun getFoldersInfo():HashMap<String,FolderBean> = folderMap
    fun getFolderInfo(folderPath:String): FolderBean? = folderMap.get(folderPath)

}