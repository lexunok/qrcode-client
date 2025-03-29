package com.lex.qr.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import com.lex.qr.R
import kotlinx.coroutines.delay

class TimerWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"
        val channel = NotificationChannel(
            channelId,
            "Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.baseline_qr_code_24)
            .setContentTitle("Прошло 30 минут")
            .setContentText("Теперь вы можете оценить пару!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}