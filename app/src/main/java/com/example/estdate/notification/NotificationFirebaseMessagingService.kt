package com.example.estdate.notification

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
}