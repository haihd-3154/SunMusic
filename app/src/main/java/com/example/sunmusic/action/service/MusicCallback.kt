package com.example.sunmusic.action.service

import com.example.sunmusic.data.model.Song

interface MusicCallback {
    fun startSong(song: Song)
    fun playSong()
    fun pauseSong()
    fun stopSong()
    fun updateTime(time: Int)
    fun isPlaying(): Boolean
}
