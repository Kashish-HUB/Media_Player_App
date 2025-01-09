package com.example.mediaplayer

data class Song(
    val title: String,
    val filePath: String,
    val songId: Long,
    val albumArt: String? = null
)

