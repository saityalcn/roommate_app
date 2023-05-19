package com.example.estdate.models

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlin.time.Duration

class Student {
    lateinit var uid: String
    lateinit var name: String
    lateinit var surname: String
    lateinit var department: String
    lateinit var grade: String
    lateinit var state: String
    lateinit var distance: String
    lateinit var duration: String
    lateinit var contactEmail: String
    lateinit var contactPhone: String
    lateinit var profileImageUrl: String

    constructor(
        name: String,
        surname: String,
        uid: String,
        department: String,
        grade: String,
        state: String,
        distance: String,
        duration: String,
        contactEmail: String,
        contactPhone: String,
        profileImageUrl: String
    ){
        this.name = name
        this.surname = surname
        this.uid = uid
        this.department = department
        this.grade = grade
        this.state = state
        this.distance = distance
        this.duration = duration
        this.contactEmail = contactEmail
        this.contactPhone = contactPhone
        this.profileImageUrl = profileImageUrl
    }

    fun toMap(): HashMap<String, Any>{
        return hashMapOf(
            "name" to name,
            "surname" to surname,
            "uid" to uid,
            "department" to department,
            "grade" to grade,
            "state" to state,
            "distance" to distance,
            "duration" to duration,
            "contactEmail" to contactEmail,
            "contactPhone" to contactPhone,
            "profileImageUrl" to profileImageUrl
        )
    }

    companion object{
        fun toObject(map: QueryDocumentSnapshot): Student{
            return Student(
                map["name"] as String,
                map["surname"] as String,
                map["uid" ] as String,
                map["department"] as String,
                map["grade"] as String,
                map["state" ] as String,
                map["distance"] as String,
                map["duration"] as String,
                map["contactEmail"] as String,
                map["contactPhone"] as String,
                map["profileImageUrl"] as String
            )
        }

        fun fromDocumentSnapshot(map: DocumentSnapshot): Student{
            return Student(
                map["name"] as String,
                map["surname"] as String,
                map["uid" ] as String,
                map["department"] as String,
                map["grade"] as String,
                map["state" ] as String,
                map["distance"] as String,
                map["duration"] as String,
                map["contactEmail"] as String,
                map["contactPhone"] as String,
                map["profileImageUrl"] as String
            )
        }
    }
}