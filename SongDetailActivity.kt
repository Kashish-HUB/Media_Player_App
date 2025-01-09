package com.example.mediaplayer

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentUris
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class SongDetailActivity : AppCompatActivity() {

    private lateinit var albumArtImageView: ImageView
    private lateinit var songTitleTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var songSeekBar: SeekBar
    private lateinit var currentTimeTextView: TextView
    private lateinit var totalTimeTextView: TextView
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var songId: Long = -1 // Default to -1 if songId is invalid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)

        // Initialize UI components
        albumArtImageView = findViewById(R.id.albumArtImageView)
        songTitleTextView = findViewById(R.id.songTitleTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
        songSeekBar = findViewById(R.id.songSeekBar)
        prevButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        currentTimeTextView = findViewById(R.id.currentTimeTextView)
        totalTimeTextView = findViewById(R.id.totalTimeTextView)

        // Retrieve data passed from the Intent
        val songTitle = intent.getStringExtra("songTitle")
        val songFilePath = intent.getStringExtra("songFilePath")
        songId = intent.getLongExtra("songId", -1)

        // Set the song details in the UI
        songTitleTextView.text = songTitle

        // Fetch and display album art using Glide
        val albumArtUri = getAlbumArtUri(songId)
        albumArtUri?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_default_album_art) // Placeholder
                .circleCrop()
                .into(albumArtImageView)
        } ?: run {
            albumArtImageView.setImageResource(R.drawable.ic_default_album_art)
        }

        // Set up play/pause button
        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.ic_play) // Change icon to play
            } else {
                songFilePath?.let {
                    playSong(it)
                }
                playPauseButton.setImageResource(R.drawable.ic_pause) // Change icon to pause
            }
        }

        // Set up the SeekBar
        songSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress) // Seek to the progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playSong(filePath: String) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(filePath)
        mediaPlayer.prepareAsync() // Use asynchronous prepare to avoid blocking UI thread
        mediaPlayer.setOnPreparedListener {
            it.start() // Start playback when ready
            songSeekBar.max = mediaPlayer.duration // Set SeekBar max to song duration
            totalTimeTextView.text = formatTime(mediaPlayer.duration) // Display total duration

            // Start a background thread to update the SeekBar as the song plays
            Thread {
                while (mediaPlayer.isPlaying) {
                    runOnUiThread {
                        val currentPosition = mediaPlayer.currentPosition
                        songSeekBar.progress = currentPosition // Update SeekBar progress
                        currentTimeTextView.text = formatTime(currentPosition) // Update current time
                    }
                    Thread.sleep(100) // Update every 100ms
                }
            }.start()
        }
    }

    private fun getAlbumArtUri(songId: Long): Uri? {
        if (songId == -1L) {
            return null
        }

        val uri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
        val projection = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        try {
            cursor?.let {
                if (it.moveToFirst()) {
                    val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val artworkUri = Uri.parse("content://media/external/audio/albumart")
                    return Uri.withAppendedPath(artworkUri, albumId.toString())
                }
            }
        } finally {
            cursor?.close() // Ensure cursor is always closed
        }

        return null
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // Release mediaPlayer when activity is destroyed to free memory
    }
}
