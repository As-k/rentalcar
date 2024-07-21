package com.aks.rentalcar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


private const val TAG = "RentalCarFirebaseMessag"
private const val CHANNEL_ID = "my_channel"

class RentalCarFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        var sharedPref: SharedPreferences? = null

        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        // Updated the token in SharePreference
        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Check if message contains a data payload.
        if (message.data.isNotEmpty()) {
            Log.i(TAG, "onMessageReceived: data ${message.data}")
        }
        message.data.let {
            //show the notification on status bar when car speed limit succeed from maximum permitted speed.
            if (it.containsKey("carSpeed") && it.containsKey("carMaxPermittedSpeed")) {
                val carMaxPermittedSpeed: String =
                    it["carMaxPermittedSpeed"].let { carMaxPermittedSpeed -> carMaxPermittedSpeed!! }
                val carSpeed: String = it["carSpeed"].let { carSpeed -> carSpeed!! }
                try {
                    if (carSpeed.isNotEmpty() && carMaxPermittedSpeed.isNotEmpty()) {
                        val carMaxPermittedSpeedValue = Integer.parseInt(carMaxPermittedSpeed)
                        val currentCarSpeedValue = Integer.parseInt(carSpeed)
                        if (carMaxPermittedSpeedValue <= currentCarSpeedValue) {
                            showNotification(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

    }

    private fun showNotification(data: MutableMap<String, String>) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
            )
        }

        val notificationCompat = NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle(data["title"])
            .setContentText(data["body"])
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data["body"]))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.baseline_car_crash_24)
            .build()

        notificationManager.notify(notificationID, notificationCompat)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "rentalCarChannel"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "The top rental car service available"
            enableLights(true)
            lightColor = Color.BLUE
        }
        notificationManager.createNotificationChannel(channel)
    }

}