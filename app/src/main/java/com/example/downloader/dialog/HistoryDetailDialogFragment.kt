package com.example.downloader.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.example.downloader.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HistoryDetailDialogFragment(@LayoutRes contentLayoutId: Int = R.layout.dialog_fragment_history_detail) :
    BottomSheetDialogFragment(contentLayoutId) {

    companion object {
        private const val TAG_URL = "tag_url"

        fun newInstance(url: String): HistoryDetailDialogFragment {
            val fragment = HistoryDetailDialogFragment(R.layout.dialog_fragment_history_detail)
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
        val view: View = inflater.inflate(R.layout.dialog_fragment_history_detail, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {

    }
}