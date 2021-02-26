package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.Constants.Companion.EXTRA_NAME
import com.udacity.Constants.Companion.EXTRA_STATUS
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private val notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            download()
        }

        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        createChannel()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            DownloadManager.COLUMN_STATUS

            id?.let {

                val query = DownloadManager.Query()
                query.setFilterById(id)
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(query)

                var statusValue = 1

                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_FAILED) {
                        statusValue = 0
                    }
                }

                cursor.close()

                val downloadName =
                    findViewById<RadioButton>(radio_group.checkedRadioButtonId).text.toString()
                sendMessage(downloadName, statusValue)
                enableRadio(true)
            }
            custom_button.progress = 100f
        }
    }

    private fun enableRadio(enable: Boolean) {
        radio_glide.isEnabled = enable
        radio_loadapp.isEnabled = enable
        radio_retrofit.isEnabled = enable
    }

    private fun download() {
        val url = when (radio_group.checkedRadioButtonId) {
            radio_glide.id -> URL_GLIDE
            radio_loadapp.id -> URL
            radio_retrofit.id -> URL_RETROFIT
            else -> {
                ""
            }
        }

        if (url.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_select_file), Toast.LENGTH_LONG).show()
            return
        }

        custom_button.buttonState = ButtonState.Loading
        enableRadio(false)

        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        custom_button.progress = 90f
        custom_button.isEnabled = false
    }

    private fun sendMessage(messageBody: String, status: Int) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra(EXTRA_NAME, messageBody)
        contentIntent.putExtra(EXTRA_STATUS, status)
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),
            pendingIntent
        )

        val builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(messageBody)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(action)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(NotificationCompat.PRIORITY_MAX)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "Download"
        private const val CHANNEL_DESCRIPTION = "File Downloaded"
    }

}
