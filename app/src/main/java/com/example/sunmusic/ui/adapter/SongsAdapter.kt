package com.example.sunmusic.ui.adapter

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sunmusic.R
import com.example.sunmusic.action.service.MusicService
import com.example.sunmusic.data.model.Song
import com.example.sunmusic.databinding.ItemSongLayoutBinding
import com.example.sunmusic.utils.ALBUM_EXTERNAL_URL
import com.example.sunmusic.utils.SONG_EXTRA

class SongsAdapter(private val mListener: ItemClickListener) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {
    private var sList = mutableListOf<Song>()
    private var sPosition: Int = -1
    private var mContext: Context? = null

    interface ItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position:Int) : Boolean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding = ItemSongLayoutBinding.inflate(LayoutInflater.from(mContext))
        return ViewHolder(binding,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(sList[position]) {
                binding.txtItemTitle.text = this.title
                binding.txtItemAuthor.text = this.artist
                binding.imgPlaying.visibility = View.GONE
                if (sPosition == position) {
                    binding.imgPlaying.visibility = View.VISIBLE
                    mContext?.let {
                        Glide.with(it).load(R.drawable.playing).into(binding.imgPlaying)
                    }
                } else {
                    binding.imgPlaying.visibility = View.GONE
                }
                val art = ContentUris.withAppendedId(
                    Uri.parse(ALBUM_EXTERNAL_URL),
                    this.albumUri.toLong()
                )
                mContext?.let {
                    Glide.with(it)
                        .load(art)
                        .placeholder(R.drawable.img_album_default)
                        .into(binding.imgItemAlbum)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return sList.size
    }

    fun updatePosition(nextPosition: Int) {
        val temp = sPosition
        sPosition = nextPosition
        notifyItemChanged(temp)
        notifyItemChanged(nextPosition)
    }
    fun setAdapterData(list : MutableList<Song>){
        sList = list
    }

    inner class ViewHolder(val binding: ItemSongLayoutBinding, listener: ItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener{
                listener.onItemLongClick(adapterPosition)
            }
        }
    }
}
