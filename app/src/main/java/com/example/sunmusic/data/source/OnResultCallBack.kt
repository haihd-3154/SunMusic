package com.example.sunmusic.data.source

import com.example.sunmusic.data.model.Song

interface OnResultCallBack<T>{
    fun onSuccess(listOfMedia: T)
    fun onError(message: String)
}
