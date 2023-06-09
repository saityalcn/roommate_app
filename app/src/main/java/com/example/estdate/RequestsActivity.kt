package com.example.estdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.estdate.adapters.ListAdapter
import com.example.estdate.adapters.RequestsListAdapter
import com.example.estdate.models.Student
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RequestsActivity : AppCompatActivity() {
    lateinit var studentsList: RecyclerView
    lateinit var studentAdapter: com.example.estdate.adapters.RequestsListAdapter
    lateinit var students: MutableList<Student>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)
        val db = Firebase.firestore
        val studentsDb = db.collection("students")
        val layoutManager: LinearLayoutManager = LinearLayoutManager(this@RequestsActivity,
            LinearLayoutManager.VERTICAL,false)
        val auth = Firebase.auth

        supportActionBar?.hide()


        studentsList = findViewById<RecyclerView>(R.id.requests_list)
        students = mutableListOf<Student>()


        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        studentsList.visibility = View.GONE

        studentsDb.document(auth.currentUser!!.uid).get().addOnSuccessListener {
            val std: Student = Student.fromDocumentSnapshot(it)
            for (docId in std.requests) {
                Log.d("ID", docId)
                studentsDb.document(docId).get().addOnSuccessListener {
                    students.add(Student.fromDocumentSnapshot(it))
                    Log.d("REQUESTS", std.requests.size.toString())
                    Log.d("students success", students.size.toString())
                    progressBar.visibility = View.GONE
                    studentsList.visibility = View.VISIBLE
                    studentAdapter = RequestsListAdapter(list=students)
                    studentsList.adapter = studentAdapter
                    studentsList.layoutManager = layoutManager
                }
            }
            if(std.requests.size == 0){
                progressBar.visibility = View.GONE
                studentsList.visibility = View.VISIBLE
            }
        }.addOnFailureListener{
            Log.d("FAILURE", it.toString())
        }
    }


}