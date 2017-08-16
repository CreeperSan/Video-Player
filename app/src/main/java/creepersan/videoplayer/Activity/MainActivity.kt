package creepersan.videoplayer.Activity

import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import creepersan.videoplayer.Base.BaseActivity
import creepersan.videoplayer.Helper.ContentResolverHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    private lateinit var adapter:MediaAdapter

    override fun getLayoutID(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initList()
    }

    private fun initData(){

        log("读取完了")
    }
    private fun initList(){
        adapter = MediaAdapter()
        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = adapter
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
        }
        return super.onOptionsItemSelected(item)
    }


    inner class MediaAdapter : RecyclerView.Adapter<MediaHolder>(){
        override fun onBindViewHolder(holder: MediaHolder, position: Int) {

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MediaHolder
                = MediaHolder(layoutInflater.inflate(R.layout.item_main_list,parent,false))

        override fun getItemCount(): Int = 30

    }
    inner class MediaHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        var icon:ImageView = itemView.findViewById<ImageView>(R.id.itemMainListIcon)
        var title:TextView = itemView.findViewById<TextView>(R.id.itemMainListTitle)
        var content:TextView = itemView.findViewById<TextView>(R.id.itemMainListContent)
    }
}