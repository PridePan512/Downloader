package com.example.downloader.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.downloader.R
import com.example.downloader.adapter.TaskAdapter
import com.example.downloader.dialog.ClipboardDialogFragment
import com.example.downloader.model.eventbus.UrlMessage
import com.example.downloader.utils.AndroidUtil
import com.example.library.LibHelper
import com.example.library.model.YtDlpException
import com.example.library.model.YtDlpRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadFragment : Fragment() {

    private val TAG = "DownloadFragment"
    private val TAG_SHOW_CLIPBOARD_DIALOG: String = "show_clipboard_dialog"

    private lateinit var mAdapter: TaskAdapter
    private lateinit var mUrlEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_download, container, false)
        initView(view)
        return view
    }

    override fun onStart() {
        super.onStart()
        checkClipBoard()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessage(message: UrlMessage) {
        searchUrl(message.url)
        mUrlEditText.setText(message.url)
    }

    private fun initView(view: View) {
        mUrlEditText = view.findViewById<TextInputEditText>(R.id.et_url)
        val clearTextButton = view.findViewById<ImageView>(R.id.iv_clear_edittext)
        val recyclerView = view.findViewById<RecyclerView>(R.id.v_recyclerview)

        mAdapter = TaskAdapter()
        recyclerView.adapter = mAdapter
        context?.let {
            recyclerView.layoutManager = LinearLayoutManager(it)
        }

        mUrlEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                clearTextButton.visibility = if (editable.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        })

        mUrlEditText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                context?.let {
                    AndroidUtil.hideKeyboard(it, mUrlEditText)
                    mUrlEditText.clearFocus()
                }
                searchUrl(mUrlEditText.text.toString())
                true
            } else {
                false
            }
        }

        clearTextButton.setOnClickListener {
            mUrlEditText.setText("")
        }
    }

    private fun searchUrl(url: String) {

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(context, "url为空", Toast.LENGTH_SHORT).show()
            return
        }

        if (!AndroidUtil.isValidWebUrl(url)) {
            Toast.makeText(context, "不是合法的url", Toast.LENGTH_SHORT).show()
            return
        }

        if (!AndroidUtil.isNetworkAvailable(requireContext())) {
            Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = YtDlpRequest(url)
                val videoInfo = LibHelper.getInfo(request).videoInfo
                withContext(Dispatchers.Main) {
                    mAdapter.insertTask(videoInfo)
                }

            } catch (e: YtDlpException) {
                Log.e(TAG, "initView: $e")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkClipBoard() {
        val dialogFragment = childFragmentManager.findFragmentByTag(TAG_SHOW_CLIPBOARD_DIALOG)
        if (dialogFragment is ClipboardDialogFragment) {
            dialogFragment.dismissAllowingStateLoss()
        }

        //延时 因为从安卓10开始 当app没有获取到焦点时拿不到粘贴板的内容
        lifecycleScope.launch {
            delay(500)
            context?.let {
                val textFromClipboard = AndroidUtil.getTextFromClipboard(it)

                if (AndroidUtil.isValidWebUrl(textFromClipboard)) {
                    val dialogFragment = ClipboardDialogFragment.newInstance(textFromClipboard)
                    dialogFragment.isCancelable = false
                    dialogFragment.show(childFragmentManager, TAG_SHOW_CLIPBOARD_DIALOG)
                }
            }
        }
    }

}