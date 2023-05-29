package com.example.estdate.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.estdate.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationFirebaseMessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val db = Firebase.firestore
        val auth = Firebase.auth
        if(auth.currentUser != null){
            db.collection("students").document(auth.currentUser!!.uid).update(
                mapOf("fcmToken" to token) as Map<String, String>
            )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("MESSAGE", message.data.get("message") as String)
        val CHANNEL_ID = "100"

        // Bildirim kanalını oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )


            channel.description = "Channel Description"

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }



        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data.get("title") )
            .setContentText(message.data.get("message") )
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setAutoCancel(true)

        // Bildirimi göster
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }
}