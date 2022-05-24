package com.example.sunmusic.data.source.local

import android.content.Context
import android.provider.MediaStore
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.data.source.OnResultCallBack

class LocalMusic(private val context: Context) : ILocalDataSource{

    override fun getData(callback: OnResultCallBack<MutableList<Song>>) {
        val sList = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val cursor = context.contentResolver.query(uri,null,selection,null,null)
        if (cursor != null){
            while(cursor.moveToNext()){
                with(cursor){
                    val url = if (getColumnIndex(MediaStore.Audio.Media.DATA)<0) ""
                    else cursor.getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val id =if (getColumnIndex(MediaStore.Audio.Media._ID)<0) ""
                    else getString(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title =if (getColumnIndex(MediaStore.Audio.Media.TITLE)<0) ""
                    else getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val author = if (getColumnIndex(MediaStore.Audio.Media.ARTIST)<0) ""
                    else getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val albumUri = if (cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)<0) ""
                    else getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val duration = if (cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)<0) 0
                    else getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    if (url!="") {
                        sList.add(Song(id,title,author,albumUri,url,duration))
                    }
                }
            }
        }
        if (sList.isEmpty()){
            callback.onError("")
        } else{
            callback.onSuccess(sList)
        }
    }

    companion object {
        private var instance: LocalMusic? = null
        fun getInstance(context: Context) =
            instance ?: LocalMusic(context).also { instance = it }
    }
}
