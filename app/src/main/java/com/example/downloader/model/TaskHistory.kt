package com.example.downloader.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class TaskHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var duration: Int = 0,
    var downloadTime: Long = 0,
    var uploadData: String? = null,
    var resource: String? = null,
    var title: String? = null,
    var thumbnail: String? = null,
    var uploader: String? = null,
    var url: String? = null,
    var path: String? = null
)