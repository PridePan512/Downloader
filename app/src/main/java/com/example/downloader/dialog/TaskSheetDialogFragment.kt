package com.example.downloader.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.example.downloader.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class TaskSheetDialogFragment(@LayoutRes contentLayoutId: Int = R.layout.dialog_sheet_task) :
    BottomSheetDialogFragment(contentLayoutId) {

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
        if (!TextUtils.isEmpty(url)) {
            urlTextView.text = url
        }
    }
}