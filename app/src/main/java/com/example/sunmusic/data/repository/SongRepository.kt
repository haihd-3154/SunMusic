package com.example.sunmusic.data.repository

import com.example.sunmusic.data.model.Song
import com.example.sunmusic.data.source.OnResultCallBack
import com.example.sunmusic.data.source.local.ILocalDataSource

class SongRepository(private val mILocalDataSource : ILocalDataSource) :  ILocalDataSource{
    override fun getData(callback: OnResultCallBack<MutableList<Song>>) {
        mILocalDataSource.getData(callback)
    }

    companion object {
        private var instance: SongRepository? = null
        fun getInstance(mILocalDataSource: ILocalDataSource) =
            instance ?: SongRepository(mILocalDataSource).also { instance = it }
    }
}
