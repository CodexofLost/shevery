package moe.shizuku.manager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import moe.shizuku.manager.R

object StartupNotificationManager {
    private const val CHANNEL_ID = "shevery_startup"
    private const val NOTIFICATION_ID = 1003
    private var channelCreated = false

    private fun ensureChannel(context: Context) {
        if (channelCreated) return
        channelCreated = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_startup_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setup(context: Context) {
        ensureChannel(context)
    }

    fun showProgress(context: Context, message: String) {
        show(context, message, indeterminate = true)
    }

    fun showStarted(context: Context, message: String) {
        show(context, message, indeterminate = false)
        Handler(Looper.getMainLooper()).postDelayed({
            dismiss(context)
        }, 3000)
    }

    fun showFailed(context: Context, message: String) {
        show(context, message, indeterminate = false)
    }

    private fun show(context: Context, message: String, indeterminate: Boolean) {
        ensureChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wadb_24)
            .setContentTitle(context.getString(R.string.notification_startup_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .setProgress(0, 0, indeterminate)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun dismiss(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}