package com.example.sunmusic.utils

const val LOCAL_BROADCAST_ACTION = "com.example.sunmusic.action.service"
const val NOTIFICATION_BROADCAST_ACTION = "com.example.sunmusic.action.service.notification"
const val ALBUM_EXTERNAL_URL = "content://media/external/audio/albumart"
const val ACTION_EXTRA = "action"
const val SONG_EXTRA = "song"
const val LIST_SONG_EXTRA = "songs"
const val POSITION_EXTRA = "position"
const val TIME_EXTRA = "time"
const val CHANNEL_ID= "232"
const val READ_PERMISSION_REQUEST_CODE = 111
const val NOTIFICATION_ID = 432
const val NOTIFICATION_CHANNEL_ID = "4320"
const val NOTIFICATION_CHANNEL_NAME = "Sun Music Chanel"
enum class MusicAction{
    START, PLAY, PAUSE, NEXT, PREV, TIME, STOP
}
