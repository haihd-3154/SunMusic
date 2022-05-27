package com.example.sunmusic.action.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.Toast
import com.example.sunmusic.action.broadcast.BroadcastManager
import com.example.sunmusic.action.notification.MyNotificationManager
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.utils.MusicAction
import com.example.sunmusic.utils.SONG_EXTRA

class MusicService : Service(), MusicCallback {

    private var mMediaPlayer: MediaPlayer? = null
    private var mSong: Song? = null
    private val binder = MyBinder()

    inner class MyBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val bundle = intent?.extras
        if (bundle != null) {
            val song = bundle.get(SONG_EXTRA) as Song?
            song?.let {
                mSong = it
                startSong(it)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSong()
        MyNotificationManager(this).clearNotifications()
    }

    override fun startSong(song: Song) {
        stopSong()
        mMediaPlayer = MediaPlayer.create(this, Uri.parse(song.uri))
        mMediaPlayer?.apply {
            isLooping = true
            start()
        }
        MyNotificationManager(this).sendNotification(song, isPlaying())
        BroadcastManager(this).sendLocalBroadcast(action = MusicAction.START.name, song = song)
        startSendCurrentTime()
    }

    override fun playSong() {
        mMediaPlayer?.start()
        mSong?.let {
            MyNotificationManager(this).sendNotification(it, isPlaying())
        }
    }

    override fun pauseSong() {
        mMediaPlayer?.pause()
        mSong?.let {
            MyNotificationManager(this).sendNotification(it, isPlaying())
        }
    }

    override fun stopSong() {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    override fun updateTime(time: Int) {
        mMediaPlayer?.seekTo(time)
    }

    override fun isPlaying(): Boolean {
        return if (mMediaPlayer == null) false
        else mMediaPlayer!!.isPlaying
    }

    private fun startSendCurrentTime() {
        val handler =if (Build.VERSION.SDK_INT >= 30) Handler(Looper.getMainLooper())
            else Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                handler.postDelayed(this, 1000)
                if (!isPlaying()) return
                mMediaPlayer?.let {
                    val time = it.currentPosition
                    BroadcastManager(applicationContext).sendLocalBroadcastTime(time)
                }
            }
        }, 0)
    }
}
