package com.example.downloader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.downloader.R
import com.example.downloader.model.DownloadState
import com.example.downloader.model.VideoTask
import com.example.downloader.utils.AndroidUtil
import com.example.library.model.VideoInfo
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Locale

class TaskDetectAdapter() :
    RecyclerView.Adapter<TaskDetectAdapter.MyViewHolder>() {

    var onDownloadClick: ((videoTask: VideoTask, position: Int) -> Unit)? = null

    private val mVideoTasks = ArrayList<VideoTask>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_task_detect, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val videoTask: VideoTask = mVideoTasks[position]
        holder.bindData(videoTask)
        holder.downloadImageView.setOnClickListener {
            onDownloadClick?.invoke(videoTask, position)
        }
    }

    override fun getItemCount(): Int {
        return mVideoTasks.size
    }

    fun insertTask(videoInfo: VideoInfo) {
        mVideoTasks.add(0, VideoTask(videoInfo))
        notifyItemInserted(0)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val downloadImageView = itemView.findViewById<ImageView>(R.id.iv_download)
        private val coverImageView = itemView.findViewById<ImageView>(R.id.iv_cover)
        private val titleTextView = itemView.findViewById<TextView>(R.id.tv_title)
        private val authorTextView = itemView.findViewById<TextView>(R.id.tv_author)
        private val formatTextView = itemView.findViewById<TextView>(R.id.tv_format)
        private val sizeTextView = itemView.findViewById<TextView>(R.id.tv_size)
        private val extensionTextView = itemView.findViewById<TextView>(R.id.tv_extension)
        private val durationTextView = itemView.findViewById<TextView>(R.id.tv_duration)
        private val completeImageView = itemView.findViewById<ImageView>(R.id.iv_complete)
        private val progressView = itemView.findViewById<LinearProgressIndicator>(R.id.v_progress)
        private val failedView = itemView.findViewById<View>(R.id.v_failed)
        private val failedTitleTextView = itemView.findViewById<TextView>(R.id.tv_failed_title)
        private val failedContentTextView = itemView.findViewById<TextView>(R.id.tv_failed_content)

        fun bindData(videoTask: VideoTask) {
            val videoInfo = videoTask.videoInfo
            Glide
                .with(itemView.context)
                .load(videoInfo.thumbnail)
                .centerCrop()
                .into(coverImageView)
            titleTextView.text = videoInfo.title
            authorTextView.text = videoInfo.uploader
            extensionTextView.text = videoInfo.ext?.uppercase(Locale.getDefault())
            formatTextView.text = videoInfo.format
            durationTextView.text = AndroidUtil.formatDuration(videoInfo.duration.toLong())
            durationTextView.background.alpha = 180
            sizeTextView.text = AndroidUtil.getHumanFriendlyByteCount(videoInfo.getSize())

            when (videoTask.state) {
                DownloadState.NOT_DOWNLOAD -> {
                    progressView.visibility = View.GONE
                    completeImageView.visibility = View.GONE
                    downloadImageView.visibility = View.VISIBLE
                    failedView.visibility = View.GONE
                }

                DownloadState.DOWNLOADING -> {
                    progressView.visibility = View.VISIBLE
                    completeImageView.visibility = View.GONE
                    downloadImageView.visibility = View.GONE
                    failedView.visibility = View.GONE
                }

                DownloadState.DOWNLOADED -> {
                    progressView.visibility = View.GONE
                    completeImageView.visibility = View.VISIBLE
                    downloadImageView.visibility = View.GONE
                    failedView.visibility = View.GONE
                }

                DownloadState.DOWNLOAD_FAILED -> {
                    progressView.visibility = View.GONE
                    completeImageView.visibility = View.GONE
                    downloadImageView.visibility = View.VISIBLE
                    failedTitleTextView.text = videoTask.error?.title
                    failedContentTextView.text = videoTask.error?.content
                    failedView.visibility = View.VISIBLE
                }
            }
        }
    }
}