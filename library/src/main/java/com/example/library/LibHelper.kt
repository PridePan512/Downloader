package com.example.library

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.library.model.YtDlpRequest
import com.example.library.model.YtDlpResponse
import com.example.library.utils.FileUtils
import com.example.library.utils.StreamGobbler
import com.example.library.utils.StreamProcessExtractor
import java.io.File
import java.io.IOException
import java.util.Collections

// TODO: 处理第一次初始化失败的情况

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

    @JvmOverloads
    fun getInfo(
        request: YtDlpRequest,
        processId: String? = null,
        callback: ((Float, Long, String) -> Unit)? = null
    ): String {
        if (!initSuccess) throw Exception("Initialize failed")
        if (processId != null && mIdProcessMap.containsKey(processId)) throw Exception("Process ID already exists")

        request.addOption("--dump-json")
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
        val ytDlpResponse: YtDlpResponse
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
            throw Exception(e)
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
            throw e
        }
        val out = outBuffer.toString()
        val err = errBuffer.toString()
        if (exitCode > 0) {
            if (processId != null && !mIdProcessMap.containsKey(processId))
                throw Exception()
            if (!request.hasOption("--dump-json") || out.isEmpty() || !request.hasOption("--ignore-errors")) {
                mIdProcessMap.remove(processId)
                throw Exception(err)
            }
        }
        mIdProcessMap.remove(processId)

        val elapsedTime = System.currentTimeMillis() - startTime
        ytDlpResponse = YtDlpResponse(command, exitCode, elapsedTime, out, err)
        return ytDlpResponse.out
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
        ENV_LD_LIBRARY_PATH += (":" + ffmpegDir.absolutePath + "/usr/lib")
        if (ffmpegDir.exists()) {
            return
        }
        ffmpegDir.mkdirs()
        val ffmpegLib = File(mNativeLibraryDir, "libffmpeg.zip.so")

        if (!ffmpegDir.exists()) {
            FileUtils.deleteFile(ffmpegDir)
            ffmpegDir.mkdirs()
            try {
                FileUtils.unzip(ffmpegLib, ffmpegDir)
            } catch (e: Exception) {
                initSuccess = false
                FileUtils.deleteFile(ffmpegDir)
                Log.e(TAG, "initFfmpeg: $e")
                throw Exception(e)
            }
        }
    }

    private fun initAria2c() {
        val aria2cDir = File(mNoBackupDir, "aria2c")
        ENV_LD_LIBRARY_PATH += (":" + aria2cDir.absolutePath + "/usr/lib")
        if (aria2cDir.exists()) {
            return
        }
        aria2cDir.mkdirs()
        val aria2cLib = File(mNativeLibraryDir, "libffmpeg.zip.so")

        if (!aria2cDir.exists()) {
            FileUtils.deleteFile(aria2cDir)
            aria2cDir.mkdirs()
            try {
                FileUtils.unzip(aria2cLib, aria2cDir)
            } catch (e: Exception) {
                initSuccess = false
                FileUtils.deleteFile(aria2cDir)
                Log.e(TAG, "initFfmpeg: $e")
                throw Exception(e)
            }
        }
    }
}