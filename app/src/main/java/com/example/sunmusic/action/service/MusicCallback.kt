package com.example.sunmusic.action.service

interface MusicCallback {
    var mPosition : Int
    fun playSong()
    fun pauseSong()
    fun nextSong()
    fun prevSong()
    fun stopSong()
    fun sendState()
    fun updateTime(time: Int)
    fun isPlaying(): Boolean
    fun currentPosition(): Int
}
