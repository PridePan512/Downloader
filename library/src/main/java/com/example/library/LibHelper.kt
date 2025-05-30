package com.example.library

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.library.model.VideoInfo
import com.example.library.model.YtDlpException
import com.example.library.model.YtDlpRequest
import com.example.library.model.YtDlpResponse
import com.example.library.utils.FileUtils
import com.example.library.utils.StreamGobbler
import com.example.library.utils.StreamProcessExtractor
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException
import java.util.Collections

object LibHelper {

    private const val TAG = "LibHelper"

    //初始化是否成功
    private var initSuccess = false

    // data/data/<package_name>/no_backup
    private var mNoBackupDir: File? = null
    private var mNativeLibraryDir: File? = null

    // ytDlp 二进制文件
    private var mYtDlpFile: File? = null

    //python 环境参数
    private var ENV_LD_LIBRARY_PATH: String? = null
    private var ENV_SSL_CERT_FILE: String? = null
    private var ENV_PYTHON_HOME: String? = null
    private var TMPDIR: String = ""

    private val mIdProcessMap = Collections.synchronizedMap(HashMap<String, Process>())

    @Synchronized
    @WorkerThread
    fun init(context: Context) {
        mNoBackupDir = context.noBackupFilesDir
        mNativeLibraryDir = File(context.applicationInfo.nativeLibraryDir)
        initPython(context)
        initYtDlp(context)
        initFfmpeg()
        initAria2c()

        initSuccess = true
    }

    @WorkerThread
    fun getVideoInfo(request: YtDlpRequest): VideoInfo {
        request.addOption("--dump-json")

        val ytDlpResponse = execute(request)
        val videoInfo: VideoInfo = try {
            ObjectMapper().readValue(ytDlpResponse.out, VideoInfo::class.java)
        } catch (e: IOException) {
            throw YtDlpException("Unable to parse video information", e.toString())
        } ?: throw YtDlpException("Failed error", "failed to fetch video information")

        return videoInfo
    }

    @WorkerThread
    fun downloadVideo(
        url: String,
        processId: String? = null,
        callback: ((Float, Long, String) -> Unit)? = null
    ) {
        val request = YtDlpRequest(url)

        request.addOption("--no-mtime")
        //使用 aria2c 作为下载器
        request.addOption("--downloader", "libaria2c.so")
        //优先选择分离的视频流（mp4）和音频流（m4a），其次选择最好的 mp4 或任何格式
        request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
        request.addOption("-o", getDownloadDir() + "/%(title)s.%(ext)s")

        execute(request, processId, callback)
    }

    private fun execute(
        request: YtDlpRequest,
        processId: String? = null,
        callback: ((Float, Long, String) -> Unit)? = null
    ): YtDlpResponse {
        if (!initSuccess) {
            throw YtDlpException("Initialize failed", "please reopen app")
        }
        if (processId != null && mIdProcessMap.containsKey(processId)) {
            throw YtDlpException("Process ID already exists", "please reopen app")
        }
        // 禁用缓存
        if (!request.hasOption("--cache-dir") || request.getOption("--cache-dir") == null) {
            request.addOption("--no-cache-dir")
        }

        if (request.buildCommand().contains("libaria2c.so")) {
            request
                .addOption("--external-downloader-args", "aria2c:--summary-interval=1")
                .addOption(
                    "--external-downloader-args",
                    "aria2c:--ca-certificate=$ENV_SSL_CERT_FILE"
                )
        }

        request.addOption("--ffmpeg-location", File(mNativeLibraryDir, "libffmpeg.so").absolutePath)
        val process: Process
        val exitCode: Int
        val outBuffer = StringBuffer() //stdout
        val errBuffer = StringBuffer() //stderr
        val startTime = System.currentTimeMillis()
        val args = request.buildCommand()
        val command: MutableList<String?> = ArrayList()
        command.addAll(
            listOf(
                File(mNativeLibraryDir, "libpython.so").absolutePath,
                mYtDlpFile!!.absolutePath
            )
        )
        command.addAll(args)
        val processBuilder = ProcessBuilder(command)
        processBuilder.environment().apply {
            this["LD_LIBRARY_PATH"] = ENV_LD_LIBRARY_PATH
            this["SSL_CERT_FILE"] = ENV_SSL_CERT_FILE
            this["PATH"] = System.getenv("PATH") + ":" + mNoBackupDir!!.absolutePath
            this["PYTHONHOME"] = ENV_PYTHON_HOME
            this["HOME"] = ENV_PYTHON_HOME
            this["TMPDIR"] = TMPDIR
        }

        process = try {
            processBuilder.start()
        } catch (e: IOException) {
            throw YtDlpException("Process start error", e.toString())
        }
        if (processId != null) {
            mIdProcessMap[processId] = process
        }
        val outStream = process.inputStream
        val errStream = process.errorStream
        val stdOutProcessor = StreamProcessExtractor(outBuffer, outStream, callback)
        val stdErrProcessor = StreamGobbler(errBuffer, errStream)
        exitCode = try {
            stdOutProcessor.join()
            stdErrProcessor.join()
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            if (processId != null) mIdProcessMap.remove(processId)
            throw YtDlpException("Process error", e.toString())
        }
        val out = outBuffer.toString()
        val err = errBuffer.toString()
        if (exitCode > 0) {
            if (processId != null && !mIdProcessMap.containsKey(processId)) {
                throw YtDlpException("Process error", "Process cancel")
            }
            if (!request.hasOption("--dump-json") || out.isEmpty() || !request.hasOption("--ignore-errors")) {
                mIdProcessMap.remove(processId)
                throw YtDlpException("Process error", err)
            }
        }
        mIdProcessMap.remove(processId)
        val elapsedTime = System.currentTimeMillis() - startTime
        return YtDlpResponse(command, exitCode, elapsedTime, out)
    }

    private fun initPython(context: Context) {
        // Python 完整环境的压缩包位置
        val pythonLib = File(mNativeLibraryDir, "libpython.zip.so")
        // Python 完整环境的解压位置
        val pythonDir = File(mNoBackupDir, "python")

        ENV_LD_LIBRARY_PATH = pythonDir.absolutePath + "/usr/lib"
        ENV_SSL_CERT_FILE = pythonDir.absolutePath + "/usr/etc/tls/cert.pem"
        ENV_PYTHON_HOME = pythonDir.absolutePath + "/usr"
        TMPDIR = context.cacheDir.absolutePath

        if (pythonDir.exists()) {
            return
        }

        pythonDir.mkdirs()
        try {
            FileUtils.unzip(pythonLib, pythonDir)

        } catch (e: Exception) {
            initSuccess = false
            FileUtils.deleteFile(pythonDir)
            Log.e(TAG, "initPython: $e")
            throw Exception(e)
        }

    }

    private fun initYtDlp(context: Context) {
        val ytDlpDir = File(mNoBackupDir, "yt_dlp")
        mYtDlpFile = File(ytDlpDir, "ytdlp")
        if (ytDlpDir.exists()) {
            return
        }
        ytDlpDir.mkdirs()

        try {
            val inputStream =
                context.assets.open("ytdlp")
            FileUtils.copyInputStreamToFile(inputStream, mYtDlpFile!!)
        } catch (e: Exception) {
            initSuccess = false
            FileUtils.deleteFile(ytDlpDir)
            Log.e(TAG, "initYtDlp: $e")
            throw Exception(e)
        }
    }

    private fun initFfmpeg() {
        val ffmpegDir = File(mNoBackupDir, "ffmpeg")
        val ffmpegLib = File(mNativeLibraryDir, "libffmpeg.zip.so")

        ENV_LD_LIBRARY_PATH += (":" + ffmpegDir.absolutePath + "/usr/lib")

        if (ffmpegDir.exists()) return
        ffmpegDir.mkdirs()

        try {
            FileUtils.unzip(ffmpegLib, ffmpegDir)
        } catch (e: Exception) {
            initSuccess = false
            FileUtils.deleteFile(ffmpegDir)
            Log.e(TAG, "initFfmpeg: $e")
            throw e
        }
    }

    private fun initAria2c() {
        val aria2cDir = File(mNoBackupDir, "aria2c")
        val aria2cLib = File(mNativeLibraryDir, "libaria2c.zip.so")

        ENV_LD_LIBRARY_PATH += (":" + aria2cDir.absolutePath + "/usr/lib")

        if (aria2cDir.exists()) return
        aria2cDir.mkdirs()

        try {
            FileUtils.unzip(aria2cLib, aria2cDir)
        } catch (e: Exception) {
            initSuccess = false
            FileUtils.deleteFile(aria2cDir)
            Log.e(TAG, "initFfmpeg: $e")
            throw e
        }
    }

    private fun getDownloadDir(): String {
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "download_test"
        )
        if (!downloadsDir.exists()) downloadsDir.mkdir()
        return downloadsDir.absolutePath
    }
}