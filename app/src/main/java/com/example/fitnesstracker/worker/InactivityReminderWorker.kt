package com.example.fitnesstracker.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnesstracker.FitnessTrackerApplication
import com.example.fitnesstracker.R
import kotlinx.coroutines.flow.first

class InactivityReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val application = applicationContext as FitnessTrackerApplication
        val settings = application.settingsRepository.settings.first()

        if (!settings.notificationsEnabled) {
            return Result.success()
        }

        val latestActivity = application.activityRepository.getLatestActivity()
        val shouldNotify = latestActivity == null ||
            System.currentTimeMillis() - latestActivity.startTime >= TWENTY_FOUR_HOURS_MILLIS

        if (shouldNotify) {
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.reminder_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description =
            applicationContext.getString(R.string.reminder_notification_channel_description)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.reminder_notification_title))
            .setContentText(applicationContext.getString(R.string.reminder_notification_body))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "inactivity_reminders"
        private const val NOTIFICATION_ID = 2001
        private const val TWENTY_FOUR_HOURS_MILLIS = 24L * 60L * 60L * 1000L
    }
}
