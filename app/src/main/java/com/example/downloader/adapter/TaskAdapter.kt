package com.example.downloader.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.downloader.R
import com.example.downloader.utils.AndroidUtil
import com.example.library.LibHelper
import com.example.library.model.VideoInfo
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class TaskAdapter(private val mLifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

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
        holder.bindData(mVideoTasks[position], mLifecycleOwner)
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
        private val complete = itemView.findViewById<ImageView>(R.id.iv_complete)
        private val progress = itemView.findViewById<LinearProgressIndicator>(R.id.v_progress)

        fun bindData(videoInfo: VideoInfo, lifecycleOwner: LifecycleOwner) {
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
            size.text = AndroidUtil.getHumanFriendlyByteCount(videoInfo.getSize())

            download.setOnClickListener {
                if (TextUtils.isEmpty(videoInfo.webpageUrl)) {
                    return@setOnClickListener
                }
                download.visibility = View.GONE
                progress.visibility = View.VISIBLE
                lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    LibHelper.downloadVideo(
                        videoInfo.webpageUrl!!,
                        processId = null
                    ) { _, _, line ->
                        // TODO: 这里的进度不太准确 暂时先不显示进度
                        if (line.startsWith("[Merger]")) {
                            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                progress.visibility = View.GONE
                                complete.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }
}