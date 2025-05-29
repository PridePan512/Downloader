package com.example.downloader.dialog

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import com.example.downloader.R
import com.example.library.LibHelper
import com.example.library.model.YtDlpException
import com.example.library.model.YtDlpRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TaskSheetDialogFragment(@LayoutRes contentLayoutId: Int = R.layout.dialog_sheet_task) :
    BottomSheetDialogFragment(contentLayoutId) {

    private val TAG = "TaskSheetDialogFragment"

    companion object {
        private const val TAG_URL = "tag_url"

        fun newInstance(url: String): TaskSheetDialogFragment {
            val fragment = TaskSheetDialogFragment(R.layout.dialog_sheet_task)
            val bundle = Bundle()
            bundle.putString(TAG_URL, url)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.dialog_sheet_task, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val urlTextView = view.findViewById<TextView>(R.id.tv_url)

        val url = arguments?.getString(TAG_URL)
        if (TextUtils.isEmpty(url)) {
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = YtDlpRequest(url.toString())
                val videoInfo = LibHelper.getVideoInfo(request)
                withContext(Dispatchers.Main) {
                    //urlTextView.text = LibHelper.getInfo(request)
                    Log.i("test", "videoInfo.id: " + videoInfo.id)
                    Log.i("test", "videoInfo.fullTitle: " + videoInfo.fullTitle)
                    Log.i("test", "videoInfo.title: " + videoInfo.title)
                    Log.i("test", "videoInfo.uploadDate: " + videoInfo.uploadDate)
                    Log.i("test", "videoInfo.displayId: " + videoInfo.displayId)
                    Log.i("test", "videoInfo.duration: " + videoInfo.duration)
                    Log.i("test", "videoInfo.description: " + videoInfo.description)
                    Log.i("test", "videoInfo.thumbnail: " + videoInfo.thumbnail)
                    Log.i("test", "videoInfo.license: " + videoInfo.license)
                    Log.i("test", "videoInfo.extractor: " + videoInfo.extractor)
                    Log.i("test", "videoInfo.extractorKey: " + videoInfo.extractorKey)
                    Log.i("test", "videoInfo.viewCount: " + videoInfo.viewCount)
                    Log.i("test", "videoInfo.likeCount: " + videoInfo.likeCount)
                    Log.i("test", "videoInfo.dislikeCount: " + videoInfo.dislikeCount)
                    Log.i("test", "videoInfo.repostCount: " + videoInfo.repostCount)
                    Log.i("test", "videoInfo.averageRating: " + videoInfo.averageRating)
                    Log.i("test", "videoInfo.uploaderId: " + videoInfo.uploaderId)
                    Log.i("test", "videoInfo.uploader: " + videoInfo.uploader)
                    Log.i("test", "videoInfo.playerUrl: " + videoInfo.playerUrl)
                    Log.i("test", "videoInfo.webpageUrl: " + videoInfo.webpageUrl)
                    Log.i("test", "videoInfo.webpageUrlBasename: " + videoInfo.webpageUrlBasename)
                    Log.i("test", "videoInfo.resolution: " + videoInfo.resolution)
                    Log.i("test", "videoInfo.width: " + videoInfo.width)
                    Log.i("test", "videoInfo.height: " + videoInfo.height)
                    Log.i("test", "videoInfo.format: " + videoInfo.format)
                    Log.i("test", "videoInfo.formatId: " + videoInfo.formatId)
                    Log.i("test", "videoInfo.ext: " + videoInfo.ext)
                    Log.i("test", "videoInfo.fileSize: " + videoInfo.fileSize)
                    Log.i("test", "videoInfo.fileSizeApproximate: " + videoInfo.fileSizeApproximate)
                    Log.i("test", "videoInfo.manifestUrl: " + videoInfo.manifestUrl)
                    Log.i("test", "videoInfo.url: " + videoInfo.url)

                    Log.i("test", "videoInfo.httpHeaders.size: " + videoInfo.httpHeaders?.size)
                    Log.i("test", "videoInfo.categories.size: " + videoInfo.categories?.size)
                    Log.i("test", "videoInfo.tags.size: " + videoInfo.tags?.size)
                    Log.i(
                        "test",
                        "videoInfo.requestedFormats.size: " + videoInfo.requestedFormats?.size
                    )
                    Log.i("test", "videoInfo.formats.size: " + videoInfo.formats?.size)
                    Log.i("test", "videoInfo.thumbnails.size: " + videoInfo.thumbnails?.size)

                    for (format in videoInfo.formats!!) {
                        Log.i("format", "format.url: " + format.url)
                        Log.i("format", "format.fileSize: " + format.fileSize)
                        Log.i("format", "format.fileSizeApproximate: " + format.fileSizeApproximate)
                        Log.i("format", "format.formatId: " + format.formatId)
                        Log.i("format", "format.formatNote: " + format.formatNote)
                        Log.i("format", "format.asr: " + format.asr)
                        Log.i("format", "format.tbr: " + format.tbr)
                        Log.i("format", "format.abr: " + format.abr)
                        Log.i("format", "format.format: " + format.format)
                        Log.i("format", "format.ext: " + format.ext)
                        Log.i("format", "format.preference: " + format.preference)
                        Log.i("format", "format.vcodec: " + format.vcodec)
                        Log.i("format", "format.acodec: " + format.acodec)
                        Log.i("format", "format.fps: " + format.fps)
                        Log.i("format", "format.manifestUrl: " + format.manifestUrl)
                        Log.i("format", "========================================")
                    }
                }
            } catch (e: YtDlpException) {
                Log.e(TAG, "initView: $e")
                return@launch
            }
        }
    }
}