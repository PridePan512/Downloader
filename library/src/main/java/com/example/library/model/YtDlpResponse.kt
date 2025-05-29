package com.example.library.model

class YtDlpResponse(
    val command: List<String?>,
    val exitCode: Int,
    val elapsedTime: Long,
    val out: String
)

class YtDlpException(
    title: String,
    content: String
) : Exception("$title: $content")