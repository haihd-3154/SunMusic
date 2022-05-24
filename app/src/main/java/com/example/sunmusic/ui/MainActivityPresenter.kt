package com.example.sunmusic.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sunmusic.action.service.MusicService
import com.example.sunmusic.data.source.local.LocalMusic
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.data.repository.SongRepository
import com.example.sunmusic.data.source.OnResultCallBack
import com.example.sunmusic.utils.*

class MainActivityPresenter(
    private val mView: MainActivityContract.View,
    private val songRepository: SongRepository,
    ) : MainActivityContract.Presenter {

    private lateinit var mService: MusicService
    private var localBroadcastManager: LocalBroadcastManager?=null
    private var sList = mutableListOf<Song>()
    private var mBound: Boolean = false
    private var currentSong: Song? = null
    private var seekBarTemp: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MyBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private var myLocalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(ACTION_EXTRA)) {
                MusicAction.START.name -> {
                    val song = intent.getParcelableExtra<Song>(SONG_EXTRA)
                    currentSong = song
                    song?.let {
                        mView.displaySongStarted(it, getCurrentPosition())
                        mView.displaySeekProgress(0)
                    }
                }
                MusicAction.TIME.name -> {
                    val time = intent.getIntExtra(TIME_EXTRA,0)
                    mView.displaySeekProgress(time)
                }
                else -> {}
            }
        }
    }
    private var myNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(ACTION_EXTRA)) {
                MusicAction.PAUSE.name -> {
                    handlePlayOrPause()
                }
                MusicAction.PLAY.name -> {
                    handlePlayOrPause()
                }
                MusicAction.NEXT.name -> {
                    handlePlayNext()
                }
                MusicAction.PREV.name -> {
                    handlePlayPrevious()
                }
                MusicAction.STOP.name -> {
                }
                else -> {}
            }
        }
    }

    override fun getSong(activity: AppCompatActivity) {
        if (checkReadPermission(activity.applicationContext)) {
            requestReadPermission(activity)
            mView.getSongsFailure()
        } else {
            songRepository.getData(object : OnResultCallBack<MutableList<Song>> {
                override fun onSuccess(list: MutableList<Song>) {
                    sList = list
                    mView.getSongsSuccess(sList)
                }

                override fun onError(message: String) {
                    mView.getSongsFailure()
                }
            })
        }
    }

    override fun registerReceiver(context: Context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
        localBroadcastManager?.registerReceiver(myLocalReceiver, IntentFilter(LOCAL_BROADCAST_ACTION))
        context.registerReceiver(myNotificationReceiver, IntentFilter(NOTIFICATION_BROADCAST_ACTION))
    }

    override fun unregisterReceiver(context: Context) {
        mBound = false
        localBroadcastManager?.unregisterReceiver(myLocalReceiver)
        context.unregisterReceiver(myNotificationReceiver)
    }

    override fun bindService(context: ContextWrapper) {
        Intent(context, MusicService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun unbindService(context: ContextWrapper) {
        context.unbindService(connection)
    }

    override fun handlePlayOrPause() {
        if (mService.isPlaying()) {
            mView.displaySongPaused()
            mService.pauseSong()
        } else {
            mView.displaySongPlayed()
            mService.playSong()
        }
    }

    override fun handlePlayNext() {
        val position = getCurrentPosition()
        val nextSong = if (position == sList.size - 1) {
            sList.first()
        } else if (position >= 0) {
            sList[position + 1]
        } else {
            sList.first()
        }
        mService.startSong(nextSong)
    }

    override fun handlePlayPrevious() {
        val position = getCurrentPosition()
        val previousSong = if (position == 0) {
            sList.last()
        } else if (position < sList.size && position > 0) {
            sList[position - 1]
        } else {
            sList.first()
        }
        mService.startSong(previousSong)
    }

    override fun handleSeekBarChanging() {
        seekBarTemp =  mService.isPlaying()
        mService.pauseSong()
    }

    override fun handleSeekBarChanged(position: Int) {
        if (seekBarTemp){
            mService.updateTime(position)
            mService.playSong()
        } else {
            mService.updateTime(position)
        }
    }

    private fun getCurrentPosition(): Int {
        currentSong?.let {
            return sList.indexOfFirst { song -> song.id == currentSong?.id }
        }
        return 0
    }

    private fun checkReadPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadPermission(activity : AppCompatActivity){
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_PERMISSION_REQUEST_CODE
        )
    }
}
