package com.example.estdate.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationFirebaseMessagingService: FirebaseMessagingService() {
    class MyFirebaseMessagingService : FirebaseMessagingService() {
        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            // Gelen bildirimi işleme kodunu buraya yazın
        }
    }
}