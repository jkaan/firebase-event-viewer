package com.jk.firebaseeventviewer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.EmptyCoroutineContext

class AnalyticsDebugProvider : ContentProvider() {
    @Suppress("BlockingMethodInNonBlockingContext")
    fun logCatOutput() = liveData(EmptyCoroutineContext + Dispatchers.IO) {
        Runtime.getRuntime().exec("logcat -c")
        Runtime.getRuntime().exec("logcat -v time -s FA FA-SVC")
            .inputStream
            .bufferedReader()
            .useLines { lines ->
                lines.forEach { line ->
                    emit(line)
                }
            }
    }

    override fun onCreate(): Boolean {
        createNotificationChannel()

        val regex = "Logging event \\(FE\\): ([a-z_]+), Bundle\\[\\{([a-z\\-0-9=_, ()]+)".toRegex(
            RegexOption.IGNORE_CASE
        )

        logCatOutput()
            .observeForever { log ->
                regex.find(log)?.let {
                    val (eventName, bundle) = it.destructured

                    context?.let { context ->
                        val notificationBuilder =
                            NotificationCompat.Builder(
                                context,
                                NOTIFICATION_CHANNEL
                            )
                                .setSmallIcon(R.drawable.ic_analytics)
                                .setContentTitle("Firebase Events")
                                .setContentText(bundle)
                                .setSubText(eventName)
                                .setOngoing(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setStyle(
                                    NotificationCompat.BigTextStyle()
                                        .bigText(bundle)
                                )

                        notificationBuilder.build()

                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        notificationManager.notify(
                            NOTIFICATION_ID,
                            notificationBuilder.build()
                        )
                    }
                }
            }

        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Firebase Debug Messages"
            val descriptionText = "Firebase debug messages"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("firebase-debug", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL = "firebase-debug"
    }
}