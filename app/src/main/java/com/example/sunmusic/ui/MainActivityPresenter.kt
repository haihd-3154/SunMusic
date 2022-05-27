package com.example.sunmusic.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sunmusic.action.service.MusicService
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
    private var mBound: Boolean = false
    private var currentSong: Song? = null
    private var seekBarTemp: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MyBinder
            mService = binder.getService()
            mBound = true
            mService.sendState()
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
                        mView.displaySongStarted(it, mService.mPosition)
                        mView.displaySongPausedOrPlaying(mService.isPlaying())
                        mView.displaySeekProgress(mService.currentPosition())
                    }
                }
                MusicAction.PLAY.name -> {
                    mView.displaySongPausedOrPlaying(true)
                }
                MusicAction.PAUSE.name ->{
                    mView.displaySongPausedOrPlaying(false)
                }
                MusicAction.TIME.name -> {
                    val time = intent.getIntExtra(TIME_EXTRA,0)
                    mView.displaySeekProgress(time)
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
                override fun onSuccess(listOfMedia: MutableList<Song>) {
                    mView.getSongsSuccess(listOfMedia)
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
    }

    override fun unregisterReceiver(context: Context) {
        mBound = false
        localBroadcastManager?.unregisterReceiver(myLocalReceiver)
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
            mService.pauseSong()
        } else {
            mService.playSong()
        }
    }

    override fun handlePlayNext() {
        mService.nextSong()
    }

    override fun handlePlayPrevious() {
        mService.prevSong()
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
