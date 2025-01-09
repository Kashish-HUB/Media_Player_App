package com.example.mediaplayer

import android.content.Context
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    private val songs: List<Song>,
    private val onItemClick: (Song) -> Unit,
    private val context: Context // Pass context for accessing content resolver
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.songTitle.text = song.title

        // Fetch and set the album art if available
        val albumArtUri = getAlbumArtUri(song.songId)
        albumArtUri?.let {
            holder.albumArt.setImageURI(it) // Set the URI for the album art
        } ?: run {
            // If no album art is found, set a default image
            holder.albumArt.setImageResource(R.drawable.ic_default_album_art)
        }

        holder.itemView.setOnClickListener {
            onItemClick(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val albumArt: ImageView = view.findViewById(R.id.albumArt) // ImageView for album art
    }

    private fun getAlbumArtUri(songId: Long): Uri? {
        val uri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
        val projection = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                // Creating the URI for the album art
                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                return Uri.withAppendedPath(artworkUri, albumId.toString())
            }
        }
        return null
    }
}
