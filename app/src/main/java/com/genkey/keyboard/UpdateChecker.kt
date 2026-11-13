package com.genkey.keyboard

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    private const val API = "https://api.github.com/repos/mssonamik34-arch/GENKEY/releases/latest"
    private const val CHANNEL = "genkey_updates"
    private const val PREFS = "genkey"
    private const val KEY_NOTICE = "last_update_notice"

    fun check(activity: Activity) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(API).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "GENKEY-Android")
                }
                if (connection.responseCode !in 200..299) return@Thread
                val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                val body = json.optString("body", "")
                val version = Regex("(?i)Version\\s*:\\s*([0-9]+(?:\\.[0-9]+)*)")
                    .find(body)?.groupValues?.getOrNull(1) ?: return@Thread
                val apk = json.optJSONArray("assets") ?: return@Thread
                var downloadUrl: String? = null
                for (i in 0 until apk.length()) {
                    val asset = apk.optJSONObject(i) ?: continue
                    if (asset.optString("name").endsWith(".apk", true)) {
                        downloadUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                if (downloadUrl.isNullOrBlank()) return@Thread
                val installed = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName ?: "1.0"
                if (compareVersions(version, installed) <= 0) return@Thread
                val prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                if (prefs.getString(KEY_NOTICE, "") == version) return@Thread
                prefs.edit().putString(KEY_NOTICE, version).apply()
                activity.runOnUiThread {
                    if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                    showUpdateDialog(activity, version, downloadUrl)
                    postNotification(activity, version, downloadUrl)
                }
            } catch (_: Exception) {
                // Network or release parsing errors are intentionally silent.
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun compareVersions(a: String, b: String): Int {
        val aa = a.split(".").map { it.toIntOrNull() ?: 0 }
        val bb = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(aa.size, bb.size)) {
            val d = (aa.getOrElse(i) { 0 }).compareTo(bb.getOrElse(i) { 0 })
            if (d != 0) return d
        }
        return 0
    }

    private fun showUpdateDialog(activity: Activity, version: String, url: String) {
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("নতুন GENKEY আপডেট")
            .setMessage("নতুন সংস্করণ $version পাওয়া গেছে। APK ডাউনলোড করে ইনস্টল করো।")
            .setNegativeButton("পরে") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("এখন আপডেট") { _, _ -> openDownload(activity, url) }
            .show()
    }

    private fun postNotification(activity: Activity, version: String, url: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7401)
            return
        }
        val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "GENKEY Updates", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val pending = android.app.PendingIntent.getActivity(
            activity, 7402, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val notification = NotificationCompat.Builder(activity, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("GENKEY $version আপডেট")
            .setContentText("নতুন APK ডাউনলোড করতে ট্যাপ করো")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(activity).notify(7403, notification)
        } catch (_: SecurityException) {
        }
    }

    private fun openDownload(context: Context, url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
        }
    }
}
