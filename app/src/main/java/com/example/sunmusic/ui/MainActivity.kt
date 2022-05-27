package com.example.sunmusic.ui

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sunmusic.R
import com.example.sunmusic.action.service.MusicService
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.data.repository.SongRepository
import com.example.sunmusic.data.source.local.LocalMusic
import com.example.sunmusic.databinding.ActivityMainBinding
import com.example.sunmusic.ui.adapter.SongsAdapter
import com.example.sunmusic.utils.ALBUM_EXTERNAL_URL
import com.example.sunmusic.utils.READ_PERMISSION_REQUEST_CODE
import com.example.sunmusic.utils.SONG_EXTRA

class MainActivity : AppCompatActivity(), MainActivityContract.View,
    SongsAdapter.ItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private var mMainActivityPresenter =
        MainActivityPresenter(this, SongRepository.getInstance(LocalMusic.getInstance(this)))
    private var songsAdapter = SongsAdapter(this)
    private var sList = mutableListOf<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        mMainActivityPresenter.getSong(this)
        binding.apply {
            listSong.apply {
                this.layoutManager = layoutManager
                this.adapter = songsAdapter
            }
            btnPlay.setOnClickListener {
                mMainActivityPresenter.handlePlayOrPause()
            }
            btnNext.setOnClickListener {
                mMainActivityPresenter.handlePlayNext()
            }
            btnPrevious.setOnClickListener {
                mMainActivityPresenter.handlePlayPrevious()
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {}

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    mMainActivityPresenter.handleSeekBarChanging()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let {
                        mMainActivityPresenter.handleSeekBarChanged(it.progress)
                    }
                }
            }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        mMainActivityPresenter.let {
            it.registerReceiver(this)
            it.bindService(this)
        }
    }

    override fun onStop() {
        super.onStop()
        mMainActivityPresenter.let {
            it.unbindService(this)
            it.unregisterReceiver(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, MusicService::class.java)
        stopService(serviceIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_PERMISSION_REQUEST_CODE && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            mMainActivityPresenter.getSong(this)
            songsAdapter.notifyDataSetChanged()
        }
    }

    override fun displaySongStarted(song: Song, position: Int) {
        binding.apply {
            playerBar.visibility = View.VISIBLE
            txtSongTitle.text = song.title
            txtSongAuthor.text = song.artist
            seekBar.max = song.duration
        }
        Glide.with(applicationContext).load(R.drawable.ic_pause).into(binding.btnPlay)
        val art = ContentUris.withAppendedId(
            Uri.parse(ALBUM_EXTERNAL_URL),
            song.albumUri.toLong()
        )
        Glide.with(applicationContext).load(art).placeholder(R.drawable.img_album_default)
            .into(binding.imgAlbum)
        songsAdapter.updatePosition(position)
    }

    override fun displaySongPaused() {
        Glide.with(applicationContext).load(R.drawable.ic_play).into(binding.btnPlay)
    }

    override fun displaySongPlayed() {
        Glide.with(applicationContext).load(R.drawable.ic_pause).into(binding.btnPlay)
    }

    override fun displaySongStopped() {
        binding.playerBar.visibility = View.GONE
    }

    override fun displaySeekProgress(position: Int) {
        binding.seekBar.progress = position
    }


    override fun getSongsSuccess(l: MutableList<Song>) {
        sList = l
        setAdapterData(l)
    }

    override fun getSongsFailure() {
        setAdapterData(sList)
    }

    private fun setAdapterData(list: MutableList<Song>) {
        songsAdapter.setAdapterData(list)
    }

    override fun onItemClick(position: Int) {
        val song = sList[position]
        val bundle = Bundle()
        val serviceIntent = Intent(this, MusicService::class.java)
        bundle.putParcelable(SONG_EXTRA, song)
        serviceIntent.putExtras(bundle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onItemLongClick(position: Int): Boolean {
        return true
    }
}
