package com.example.sunmusic.action.broadcast

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.utils.*

class BroadcastManager(private val context: Context) {
    fun sendLocalBroadcast(song: Song, action: String){
        val mIntent = Intent(LOCAL_BROADCAST_ACTION)
        mIntent.putExtra(SONG_EXTRA,song)
        mIntent.putExtra(ACTION_EXTRA,action)
        LocalBroadcastManager.getInstance(context).sendBroadcast(mIntent)
    }

    fun sendLocalBroadcastTime(time: Int) {
        val mIntent = Intent(LOCAL_BROADCAST_ACTION)
        mIntent.putExtra(TIME_EXTRA,time)
        mIntent.putExtra(ACTION_EXTRA, MusicAction.TIME.name)
        LocalBroadcastManager.getInstance(context).sendBroadcast(mIntent)
    }

}
