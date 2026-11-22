package com.vpnclient.app.data.model

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String?,
    val fileSize: Long = 0L,
    val htmlUrl: String = ""
)
