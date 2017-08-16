package creepersan.videoplayer.Helper

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import creepersan.videoplayer.Bean.FolderBean
import creepersan.videoplayer.Bean.VideoBean

class ContentResolverHelper(private val contentResolver: ContentResolver){
    private var folderMap = HashMap<String,FolderBean>()

    fun initData(){
        val folderNameSet = HashSet<String>()
        val folderList = ArrayList<String>()
        val cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,
                null,null,null)
        while (cursor.moveToNext()){
            val tempVideoBean = VideoBean(cursor)
            if (folderNameSet.add(tempVideoBean.folderName)){//文件夹已存在
                val folderBean = folderMap.get(tempVideoBean.folderName)
                if (folderBean==null) throw IllegalStateException("初始化储存数据的")
                folderBean.videoList.add(tempVideoBean)
            }else{//如果文件夹不存在

            }
        }
        cursor.close()
    }

}