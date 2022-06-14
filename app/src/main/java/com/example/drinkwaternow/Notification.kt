package com.example.drinkwaternow

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX

const val notificationID=1
const val channelID = "DEF_CHANNEL"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

class Notification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val textTitle = intent.getStringExtra(titleExtra)
        val textContent = intent.getStringExtra(messageExtra)

        // Create an explicit intent for an Activity in your app
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        //отложенный интент для запуска MainActivity по тапу уведомления
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)

        //задание параметров уведомления
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_outline_local_drink_24)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(PRIORITY_MAX)
            //.setSound(Uri.parse("android.resource://" + context.packageName + "/" + R.raw.notification_water_2))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationID, notification)
    }

}