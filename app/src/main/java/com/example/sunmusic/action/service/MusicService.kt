package com.example.sunmusic.action.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import com.example.sunmusic.action.broadcast.BroadcastManager
import com.example.sunmusic.action.notification.MyNotificationManager
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.utils.*

class MusicService : Service(), MusicCallback {

    override var mPosition: Int = 0
    private var mMediaPlayer: MediaPlayer? = null
    private var mList: ArrayList<Song>? = null
    private val binder = MyBinder()
    private var myNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(ACTION_EXTRA)) {
                MusicAction.PAUSE.name -> pauseSong()
                MusicAction.PLAY.name -> playSong()
                MusicAction.NEXT.name -> nextSong()
                MusicAction.PREV.name -> prevSong()
                MusicAction.STOP.name -> {}
                else -> {}
            }
        }
    }


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
            mList = bundle.get(LIST_SONG_EXTRA) as ArrayList<Song>?
            val position = bundle.get(POSITION_EXTRA) as Int?
            mList?.let {
                mPosition = position ?: 0
                startSong(it[mPosition])
            }
        }
        registerReceiver(myNotificationReceiver, IntentFilter(NOTIFICATION_BROADCAST_ACTION))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSong()
        unregisterReceiver(myNotificationReceiver)
        MyNotificationManager(this).clearNotifications()
    }

    private fun startSong(song: Song) {
        stopSong()
        mMediaPlayer = MediaPlayer.create(this, Uri.parse(song.uri))
        mMediaPlayer?.apply {
            isLooping = false
            setOnCompletionListener {
                nextSong()
            }
            start()
        }
        MyNotificationManager(this).sendNotification(song, isPlaying())
        BroadcastManager(this).sendLocalBroadcast(action = MusicAction.START.name, song = song)
        startSendCurrentTime()
    }

    override fun playSong() {
        mMediaPlayer?.start()

        mList?.let {
            BroadcastManager(this).sendLocalBroadcast(action = MusicAction.PLAY.name, song = it[mPosition])
            MyNotificationManager(this).sendNotification(it[mPosition], isPlaying())
        }
    }

    override fun pauseSong() {
        mMediaPlayer?.pause()
        mList?.let {
            BroadcastManager(this).sendLocalBroadcast(action = MusicAction.PAUSE.name, song = it[mPosition])
            MyNotificationManager(this).sendNotification(it[mPosition], isPlaying())
        }
    }

    override fun nextSong() {
        mList?.let{
            mPosition = if (mPosition == it.size - 1) {
                0
            } else if (mPosition >= 0) {
                mPosition+1
            } else {
                0
            }
            startSong(it[mPosition])
        }
    }

    override fun prevSong() {
        mList?.let{
            mPosition = if (mPosition == 0) {
                it.lastIndex
            } else if (mPosition <= it.lastIndex && mPosition > 0) {
                mPosition - 1
            } else {
                0
            }
            startSong(it[mPosition])
        }
    }

    override fun stopSong() {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    override fun sendState() {
        mList?.let {
            BroadcastManager(this).sendLocalBroadcast(action = MusicAction.START.name, song = it[mPosition])
        }
    }

    override fun updateTime(time: Int) {
        mMediaPlayer?.seekTo(time)
    }

    override fun isPlaying(): Boolean {
        return if (mMediaPlayer == null) false
        else mMediaPlayer!!.isPlaying
    }
    override fun currentPosition(): Int {
        return if (mMediaPlayer == null) 0
        else mMediaPlayer!!.currentPosition
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
