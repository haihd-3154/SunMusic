package com.example.sunmusic.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import com.example.sunmusic.data.model.Song

interface MainActivityContract {
    interface View {
        fun getSongsSuccess(l: MutableList<Song>)
        fun getSongsFailure()
        fun displaySongStarted(song: Song, position: Int)
        fun displaySongPausedOrPlaying(state : Boolean)
        fun displaySongStopped()
        fun displaySeekProgress(position: Int)
    }

    interface Presenter {
        fun getSong(activity: AppCompatActivity)
        fun handlePlayOrPause()
        fun handlePlayNext()
        fun handlePlayPrevious()
        fun handleSeekBarChanging()
        fun handleSeekBarChanged(position: Int)
        fun registerReceiver(context: Context)
        fun unregisterReceiver(context: Context)
        fun bindService(context: ContextWrapper)
        fun unbindService(context: ContextWrapper)
    }
}
