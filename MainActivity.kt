package com.example.mediaplayer

import android.Manifest
import android.content.ContentUris
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var songRecyclerView: RecyclerView
    private lateinit var songList: MutableList<Song>
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private lateinit var repeatButton: ImageButton
    private lateinit var songTitleText: TextView
    private lateinit var currentTimeText: TextView // TextView for current time
    private lateinit var totalTimeText: TextView
    private lateinit var userAccountIcon: ImageView
    private  lateinit var userAccountIcon2: ImageView // TextView for total duration
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var currentSongIndex = 0
    private var isShuffleEnabled = false
    private var isRepeatEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        songRecyclerView = findViewById(R.id.songRecyclerView)
        playPauseButton = findViewById(R.id.playPauseButton)
        seekBar = findViewById(R.id.seekBar)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        shuffleButton = findViewById(R.id.shuffleButton)
        repeatButton = findViewById(R.id.repeatButton)
        songTitleText = findViewById(R.id.songTitleText)
        currentTimeText = findViewById(R.id.currentTimeText) // Initialize current time TextView
        totalTimeText = findViewById(R.id.totalTimeText)
        userAccountIcon = findViewById(R.id.userAccountIcon)// Initialize total time TextView
        userAccountIcon.setOnClickListener {
            // Intent to navigate to UserProfileActivity
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
        userAccountIcon2 = findViewById(R.id.userAccountIcon2)
        userAccountIcon2.setOnClickListener{
            val intent = Intent(this, DisplayProfileActivity::class.java)
            startActivity(intent)
        }
        // Request permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            fetchSongs()
        }

        // Set up RecyclerView for displaying song list
        songRecyclerView.layoutManager = LinearLayoutManager(this)
        songRecyclerView.adapter = SongAdapter(songList, { song ->
            val intent = Intent(this, SongDetailActivity::class.java).apply {
                putExtra("songTitle", song.title)
                putExtra("songFilePath", song.filePath)
                putExtra("songId", song.songId)
            }
            startActivity(intent)
        }, this)

        setupMediaPlayer()

        // Set up play/pause button
        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.ic_pause)
            }
        }

        // Set up next button
        nextButton.setOnClickListener {
            playNextSong()
        }

        // Set up previous button
        prevButton.setOnClickListener {
            playPreviousSong()
        }

        // Set up shuffle button
        shuffleButton.setOnClickListener {
            isShuffleEnabled = !isShuffleEnabled
            if (isShuffleEnabled) {
                songList.shuffle()
            } else {
                songList.sortBy { it.title }
            }
            songRecyclerView.adapter?.notifyDataSetChanged()
        }

        // Set up repeat button
        repeatButton.setOnClickListener {
            isRepeatEnabled = !isRepeatEnabled
            mediaPlayer.isLooping = isRepeatEnabled
        }

        // Seek bar update for user interaction
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress) // Move media player position when user changes seekbar
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun fetchSongs() {
        val songList = mutableListOf<Song>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val albumIdIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (it.moveToNext()) {
                val title = it.getString(titleIndex)
                val filePath = it.getString(dataIndex)
                val songId = it.getLong(idIndex)
                val albumId = it.getLong(albumIdIndex)

                val albumArtUri = getAlbumArtUri(albumId)

                songList.add(Song(title, filePath, songId, albumArtUri?.toString()))
            }
        }

        this.songList = songList
    }

    private fun getAlbumArtUri(albumId: Long): Uri? {
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        return Uri.withAppendedPath(artworkUri, albumId.toString())
    }

    private fun playSong(song: Song) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(song.filePath)
        mediaPlayer.prepareAsync() // Async preparation for better UI responsiveness

        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()

            // Update UI and play state
            playPauseButton.setImageResource(R.drawable.ic_pause)

            // Update seekbar max value to match the song duration
            seekBar.max = mediaPlayer.duration
            totalTimeText.text = formatTime(mediaPlayer.duration) // Set total time

            // Start background thread to update seekbar during song playback
            updateSeekBar()
        }

        songTitleText.text = song.title
    }

    private fun updateSeekBar() {
        Thread {
            while (mediaPlayer.isPlaying) {
                runOnUiThread {
                    val currentPosition = mediaPlayer.currentPosition
                    seekBar.progress = currentPosition // Update SeekBar progress
                    currentTimeText.text = formatTime(currentPosition) // Update current time text
                }
                Thread.sleep(1000) // Update every second
            }
        }.start()
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds) // Format as MM:SS
    }

    private fun playNextSong() {
        currentSongIndex = if (currentSongIndex + 1 < songList.size) currentSongIndex + 1 else 0
        val nextSong = songList[currentSongIndex]
        playSong(nextSong)
        songTitleText.text = nextSong.title
    }

    private fun playPreviousSong() {
        currentSongIndex = if (currentSongIndex - 1 >= 0) currentSongIndex - 1 else songList.size - 1
        val prevSong = songList[currentSongIndex]
        playSong(prevSong)
        songTitleText.text = prevSong.title
    }

    private fun setupMediaPlayer() {
        mediaPlayer.setOnCompletionListener {
            if (!isRepeatEnabled) {
                playNextSong()
            }
        }
    }
}
