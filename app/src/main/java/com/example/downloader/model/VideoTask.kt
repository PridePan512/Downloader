package com.example.downloader.model

import com.example.library.model.VideoInfo

class VideoTask(val videoInfo: VideoInfo, var state: Int = 0)

class DownloadState(
){
    companion object{
        public val notDownload: Int = 0
        public val downloading: Int = 1
        public val downloaded: Int = 2
    }
}