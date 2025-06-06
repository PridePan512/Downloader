package com.example.downloader.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.downloader.R
import com.example.downloader.model.TaskHistory
import com.example.downloader.utils.AndroidUtil
import java.io.File

class TaskHistoryAdapter() : RecyclerView.Adapter<TaskHistoryAdapter.MyViewHolder>() {

    private lateinit var mTaskHistories: ArrayList<TaskHistory>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_task_history, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.bindData(mTaskHistories[position])
    }

    override fun getItemCount(): Int {
        return mTaskHistories.size
    }

    fun setData(taskHistories: ArrayList<TaskHistory>) {
        mTaskHistories = taskHistories
    }

    fun insertItem(history: TaskHistory) {
        mTaskHistories.add(0, history)
        notifyItemInserted(0)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverImageView = itemView.findViewById<ImageView>(R.id.iv_cover)
        private val titleTextView = itemView.findViewById<TextView>(R.id.tv_title)
        private val uploaderTextView = itemView.findViewById<TextView>(R.id.tv_uploader)
        private val sizeTextView = itemView.findViewById<TextView>(R.id.tv_size)
        private val durationTextView = itemView.findViewById<TextView>(R.id.tv_duration)
        private val unavailableTextView = itemView.findViewById<TextView>(R.id.tv_unavailable)

        fun bindData(taskHistory: TaskHistory) {
            Glide
                .with(itemView.context)
                .load(taskHistory.thumbnail)
                .centerCrop()
                .into(coverImageView)
            titleTextView.text = taskHistory.title
            uploaderTextView.text = taskHistory.uploader
            durationTextView.text = AndroidUtil.formatDuration(taskHistory.duration.toLong())
            durationTextView.background.alpha = 180

            if (!TextUtils.isEmpty(taskHistory.path)) {
                val file = File(taskHistory.path!!)
                if (file.exists()) {
                    unavailableTextView.visibility = View.GONE
                    sizeTextView.visibility = View.VISIBLE
                    sizeTextView.text = AndroidUtil.getHumanFriendlyByteCount(file.length())

                } else {
                    unavailableTextView.visibility = View.VISIBLE
                    sizeTextView.visibility = View.GONE
                }
            }
        }
    }
}