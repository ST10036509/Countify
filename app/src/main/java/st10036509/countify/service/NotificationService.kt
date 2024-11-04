/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 03/11/2024
Last Modified: 03/11/2024
 */

package st10036509.countify.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import st10036509.countify.R

object NotificationService {

    private const val CHANNEL_ID = "counter_reset_channel" //channel id
    private const val CHANNEL_NAME = "Counter Reset Notifications" //human readable channel name

    // method to setup a notification channel for sending notifications to the system UI
    fun createNotificationChannel(context: Context) {
        //build the channel schema
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel description"
        }
        //setup a manager for system notifications
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel) //create channel with schema
        Log.i("Notifications:", "Notification Channel Created")
    }

    // method to show notifications utilising the notification channel
    fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        Log.i("Notifications:", "Notification sent")

        //build notification using NotificationCompat
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.countify_logo) //icon
            .setContentTitle(title) //title
            .setContentText(message) //message
            .setPriority(NotificationCompat.PRIORITY_HIGH) //notification priority
            .setAutoCancel(true) //auto hide
            .build() //build notification

        //use notification channel to the system UI using the notification channel
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________