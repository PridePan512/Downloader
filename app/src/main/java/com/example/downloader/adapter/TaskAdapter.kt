package com.example.downloader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.downloader.R
import com.example.downloader.utils.AndroidUtil
import com.example.library.model.VideoInfo
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Locale

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    private val mVideoTasks = ArrayList<VideoInfo>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.bindData(mVideoTasks[position])
    }

    override fun getItemCount(): Int {
        return mVideoTasks.size
    }

    fun insertTask(videoInfo: VideoInfo) {
        mVideoTasks.add(0, videoInfo)
        notifyItemInserted(0)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cover = itemView.findViewById<ImageView>(R.id.iv_cover)
        private val title = itemView.findViewById<TextView>(R.id.tv_title)
        private val author = itemView.findViewById<TextView>(R.id.tv_author)
        private val format = itemView.findViewById<TextView>(R.id.tv_format)
        private val size = itemView.findViewById<TextView>(R.id.tv_size)
        private val extension = itemView.findViewById<TextView>(R.id.tv_extension)
        private val duration = itemView.findViewById<TextView>(R.id.tv_duration)
        private val download = itemView.findViewById<ImageView>(R.id.iv_download)
        private val progress = itemView.findViewById<LinearProgressIndicator>(R.id.v_progress)

        fun bindData(videoInfo: VideoInfo) {
            Glide
                .with(itemView.context)
                .load(videoInfo.thumbnail)
                .centerCrop()
                .into(cover)
            title.text = videoInfo.title
            author.text = videoInfo.uploader
            extension.text = videoInfo.ext?.uppercase(Locale.getDefault())
            format.text = videoInfo.format
            duration.text = AndroidUtil.formatDuration(videoInfo.duration.toLong())
            duration.background.alpha = 180
            size.text = AndroidUtil.getHumanFriendlyByteCount(
                if (videoInfo.fileSize == 0L) {
                    videoInfo.fileSizeApproximate
                } else {
                    videoInfo.fileSize
                }
            )

            download.setOnClickListener {
                progress.visibility = View.VISIBLE
            }
        }
    }
}