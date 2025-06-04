package com.example.downloader.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.downloader.MyApplication
import com.example.downloader.R
import com.example.downloader.adapter.TaskHistoryAdapter
import com.example.downloader.model.TaskHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HistoryFragment : Fragment() {

    private lateinit var mAdapter: TaskHistoryAdapter

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        initView(view)
        return view
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessage(taskHistory: TaskHistory) {
        mAdapter.insertItem(taskHistory)
    }

    private fun initView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.v_recyclerview)
        mAdapter = TaskHistoryAdapter()
        context?.let {
            recyclerView.layoutManager = LinearLayoutManager(it)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // TODO: 这里考虑使用分页加载 而不是一次性加载全部
            val histories = MyApplication.database.historyDao().queryAll()
            withContext(Dispatchers.Main) {
                mAdapter.setData(ArrayList<TaskHistory>(histories))
                recyclerView.adapter = mAdapter
            }
        }
    }
}