package com.example.downloader.activity

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.downloader.R
import com.example.downloader.dialog.TaskSheetDialogFragment
import com.example.downloader.utils.AndroidUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            // TODO: 可以增加动画 提高平滑度
            val keyboardHeight: Int = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val downloadButton = findViewById<FloatingActionButton>(R.id.fab_download)

            val layoutParams: ConstraintLayout.LayoutParams =
                downloadButton.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomMargin =
                (keyboardHeight + Resources.getSystem().displayMetrics.density * 20).toInt()
            downloadButton.layoutParams = layoutParams
            insets
        }

        initView()
    }

    private fun initView() {
        val urlEditText = findViewById<TextInputEditText>(R.id.et_url)
        val clearButton = findViewById<ImageView>(R.id.iv_clear_edittext)
        clearButton.setOnClickListener {
            urlEditText.setText("")
        }
        urlEditText.addTextChangedListener(object : TextWatcher {
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
                clearButton.visibility = if (editable.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        })

        findViewById<FloatingActionButton>(R.id.fab_paste).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip

            val textFromClipboard = if (clipData != null && clipData.itemCount > 0) {
                clipData.getItemAt(0).coerceToText(this).toString()
            } else {
                ""
            }
            if (textFromClipboard.isEmpty()) {
                Toast.makeText(this, "粘贴板为空", Toast.LENGTH_SHORT).show()

            } else {
                urlEditText.requestFocus()
                urlEditText.setText(textFromClipboard)
            }
        }

        findViewById<FloatingActionButton>(R.id.fab_download).setOnClickListener {
            //强制关闭软键盘
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(urlEditText.windowToken, 0)

            if (!AndroidUtil.isNetworkAvailable(this)) {
                Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(urlEditText.text)) {
                Toast.makeText(this, "url为空", Toast.LENGTH_SHORT).show()

            } else {
                val dialogFragment =
                    TaskSheetDialogFragment.newInstance(urlEditText.text.toString())
                dialogFragment.show(supportFragmentManager, "TaskSheetDialogFragment")
//                lifecycleScope.launch {
//
//                    withContext(Dispatchers.IO) {
//
//                    }
//                }
                // TODO: 当api<29时，需要申请 WRITE_EXTERNAL_STORAGE 和 READ_EXTERNAL_STORAGE 权限
                // TODO: 当前暂时不考虑低版本的情况 只处理安卓10及以上版本
            }
        }

        findViewById<ImageView>(R.id.iv_setting).setOnClickListener {
            // TODO: 设置页面
        }
        findViewById<ImageView>(R.id.iv_history).setOnClickListener {
            // TODO: 历史任务页面
        }
    }
}