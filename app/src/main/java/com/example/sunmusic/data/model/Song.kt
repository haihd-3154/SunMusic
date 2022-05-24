package com.example.sunmusic.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    var id: String,
    var title: String,
    var artist: String,
    var albumUri: String,
    var uri: String,
    var duration: Int
) : Parcelable
