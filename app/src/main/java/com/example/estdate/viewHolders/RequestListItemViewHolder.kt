package com.example.estdate.viewHolders

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.estdate.ProfileActivity
import com.example.estdate.R
import com.example.estdate.models.Student
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class RequestListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindItems(student: Student){
        val db = Firebase.firestore
        val auth = Firebase.auth

        itemView.findViewById<TextView>(R.id.textViewGraduteName).setText(student.name + " " + student.surname)

        Glide.with(itemView)
            .load(Uri.parse(student.profileImageUrl))
            .into(itemView.findViewById<ImageView>(R.id.imageView))


        itemView.findViewById<TextView>(R.id.textViewState).text = student.state

        itemView.findViewById<ImageView>(R.id.imageViewApprove).setOnClickListener{
            Toast.makeText(itemView.context,"Eşleşme isteği onaylandı.", Toast.LENGTH_LONG)

            Log.d("TAG", "APPROVE")

            var studentsRef = db.collection("students").document(auth.currentUser!!.uid)
            studentsRef.update(
                mapOf("state" to "Aramıyor") as Map<String, String>
            )

            db.collection("students").document(student.uid).update(
                mapOf("state" to "Aramıyor") as Map<String, String>
            )

            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(studentsRef)
                val requests = userSnapshot.get("requests") as MutableList<String>?

                if (requests != null) {
                    val elementToRemove = student.uid
                    requests.remove(elementToRemove)
                    transaction.update(studentsRef, "requests", requests)
                }

                null
            }.addOnSuccessListener {
                Toast.makeText(itemView.context,"Eşleşme isteği onaylanması başarılı..", Toast.LENGTH_LONG)
                sendPushNotification(student, "Eşleşme talebiniz kabul edildi.")
                (itemView.context as Activity).finish()
            }
        }

        itemView.findViewById<ImageView>(R.id.imageViewDecline).setOnClickListener{
            Toast.makeText(itemView.context,"Eşleşme isteği onaylandı.", Toast.LENGTH_LONG)

            Log.d("TAG", "APPROVE")

            var studentsRef = db.collection("students").document(auth.currentUser!!.uid)
            studentsRef.update(
                mapOf("state" to "Aramıyor") as Map<String, String>
            )

            db.collection("students").document(student.uid).update(
                mapOf("state" to "Aramıyor") as Map<String, String>
            )

            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(studentsRef)
                val requests = userSnapshot.get("requests") as MutableList<String>?

                if (requests != null) {
                    val elementToRemove = student.uid
                    requests.remove(elementToRemove)
                    transaction.update(studentsRef, "requests", requests)
                }

                null
            }.addOnSuccessListener {
                sendPushNotification(student, "Eşleşme talebiniz reddedildi.")
                (itemView.context as Activity).finish()
            }
        }
    }
    private fun sendPushNotification(std: Student, message: String) {
        val auth = Firebase.auth
        val notificationTitle = "Eşleşme Cevabı"
        val notificationMessage =  message

        val notification = JSONObject()
        val notificationData = JSONObject()

        try {
            notificationData.put("title", notificationTitle)
            notificationData.put("message", notificationMessage)
            notification.put("to", std.fcmToken)
            notification.put("data", notificationData)
        } catch (e: JSONException) {
            Log.e("TAG", "Failed to create notification JSON", e)
        }

        val requestBody = notification.toString()
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .addHeader("Authorization", "Bearer AAAA1EPifGc:APA91bH8pdyJxaZYrVB250hf11adOj-yD2V9YvqS2VQ-ZjMnn2jYRJnmYsV3Nzcyy4DNSINv1ntbr3lBVpxV9XDNx8dIlLPJFGmc9TphcLpEbCeEQIlcl17VofDgO7tQ581J1VK3E3Bo")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", "Failed to send push notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("TAG", "Successfully sent push notification")
                } else {
                    Log.e("TAG", "Failed to send push notification. Response: ${response.body?.string()}")
                }
            }
        })
    }
}