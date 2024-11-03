package st10036509.countify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "No Title"
        val message = intent?.getStringExtra("message") ?: "No Message"

        // Broadcast the notification data to the UI
        val updateIntent = Intent("NotificationUpdate")
        updateIntent.putExtra("title", title)
        updateIntent.putExtra("message", message)
        context?.sendBroadcast(updateIntent)
    }
}