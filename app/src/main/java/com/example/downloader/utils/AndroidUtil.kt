package com.example.downloader.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.text.format.DateUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresPermission
import java.io.File
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

object AndroidUtil {

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun dpToPx(dp: Int): Int {
        val density = Resources.getSystem().displayMetrics.density
        val value = (dp * density).roundToInt()
        return value
    }

    fun getHumanFriendlyByteCount(bytes: Long, decimalPlaces: Int = 1): String {
        val unit = 1024
        if (bytes == 0L) {
            return "0 KB"
        }
        if (bytes < unit) {
            return "$bytes B"
        }

        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1]
        val formattedValue = String.format(
            Locale.getDefault(),
            "%.${decimalPlaces}f %sB",
            bytes / unit.toDouble().pow(exp.toDouble()),
            pre
        )
        return formattedValue
    }

    fun formatDuration(
        seconds: Long,
        withColon: Boolean = true,
        withDay: Boolean = false,
        dayFormat: String? = null
    ): String {
        val dayInSeconds = DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS
        val hourInSeconds = DateUtils.HOUR_IN_MILLIS / DateUtils.SECOND_IN_MILLIS

        return if (withDay && seconds >= dayInSeconds) {
            val format = if (withColon) "$dayFormat %02d:%02d:%02d" else "$dayFormat %02d%02d%02d"
            String.format(
                Locale.getDefault(),
                format,
                seconds / (3600 * 24),
                (seconds % (24 * 3600)) / 3600,
                (seconds % 3600) / 60,
                seconds % 60
            )
        } else if (seconds >= hourInSeconds) {
            val format = if (withColon) "%02d:%02d:%02d" else "%02d%02d%02d"
            String.format(
                Locale.getDefault(),
                format,
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60
            )
        } else {
            val format = if (withColon) "%02d:%02d" else "%02d%02d"
            String.format(
                Locale.getDefault(),
                format,
                (seconds % 3600) / 60,
                seconds % 60
            )
        }
    }

    fun getTextFromClipboard(context: Context): String {
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip

        return if (clipData != null && clipData.itemCount > 0) {
            clipData.getItemAt(0).coerceToText(context).toString()
        } else {
            ""
        }
    }

    fun clearClipboard(context: Context) {
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val emptyClipData = ClipData.newPlainText("", "")
        clipboard.setPrimaryClip(emptyClipData)
    }

    fun isValidWebUrl(text: String): Boolean {
        if (text.isEmpty()) {
            return false
        }
        val urlPattern = Patterns.WEB_URL
        return urlPattern.matcher(text).matches()
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getDownloadDir(): String {
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "download_test"
        )
        if (!downloadsDir.exists()) downloadsDir.mkdir()
        return downloadsDir.absolutePath
    }
}