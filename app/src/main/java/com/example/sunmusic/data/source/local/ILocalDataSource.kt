package com.example.sunmusic.data.source.local

import com.example.sunmusic.data.model.Song
import com.example.sunmusic.data.source.OnResultCallBack

interface ILocalDataSource {
    fun getData(callback: OnResultCallBack<MutableList<Song>>)
}
