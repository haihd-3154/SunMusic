package com.example.sunmusic.action.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadata
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.sunmusic.R
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.utils.*
import kotlin.random.Random


class MyNotificationManager(private val service: Service) {

    private val notificationManager = NotificationManagerCompat.from(service)

    private fun getPendingIntent(action: String): PendingIntent? {
        Intent(NOTIFICATION_BROADCAST_ACTION).also {
            it.putExtra(ACTION_EXTRA, action)
            return PendingIntent.getBroadcast(
                service.applicationContext,
                Random.nextInt(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    fun sendNotification(song: Song, isPlaying: Boolean) {
        val subText = "Play ${song.title}"
        val mediaSessionTag = "Sun Music Notification"
        val mediaSession = MediaSessionCompat(service, mediaSessionTag).apply {
            setMetadata(
                MediaMetadataCompat.Builder().apply {
                    putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
                    putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
                }.build()
            )
        }
        val art = ContentUris.withAppendedId(
            Uri.parse(ALBUM_EXTERNAL_URL),
            song.albumUri.toLong()
        )
        val bitmap : Bitmap = try {
            when {
                Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                    service.contentResolver,
                    art
                )
                else -> {
                    val source = ImageDecoder.createSource(service.contentResolver, art)
                    ImageDecoder.decodeBitmap(source)
                }
            }
        } catch (e: Exception){
            BitmapFactory.decodeResource(service.resources, R.drawable.img_album_default)
        }
        val notification = NotificationCompat.Builder(service, CHANNEL_ID).apply {
            setChannelId(NOTIFICATION_CHANNEL_ID)
            setContentTitle(song.title)
            setContentText(song.artist)
            setSubText(subText)
            setSmallIcon(R.drawable.ic_music_notification)
            addAction(R.drawable.ic_previous,service.getString(R.string.previous_song), getPendingIntent(MusicAction.PREV.name))
            if (isPlaying){
                addAction(R.drawable.ic_pause,service.getString(R.string.play_pause), getPendingIntent(MusicAction.PAUSE.name))
            } else {
                addAction(R.drawable.ic_play,service.getString(R.string.play_pause), getPendingIntent(MusicAction.PLAY.name))
            }
            addAction(R.drawable.ic_next,service.getString(R.string.next_song), getPendingIntent(MusicAction.NEXT.name))
            setLargeIcon(bitmap)
            setAutoCancel(false)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setStyle(
                MediaStyle()
                    .setShowCancelButton(true)
                    .setMediaSession(mediaSession.sessionToken)
            )
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()
        notificationManager.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                )
                createNotificationChannel(mChannel)
                service.startForeground(NOTIFICATION_ID, notification)
            }
            else{
                notify(NOTIFICATION_ID, notification)
            }
        }

    }

    fun clearNotifications() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

}
