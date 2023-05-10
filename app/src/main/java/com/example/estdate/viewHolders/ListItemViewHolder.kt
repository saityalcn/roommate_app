package com.example.estdate.viewHolders

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.estdate.LoginActivity
import com.example.estdate.R
import com.example.estdate.models.Student
import java.util.Date

class ListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindItems(student: Student){
        itemView.findViewById<TextView>(R.id.textViewGraduteName).setText(student.name + " " + student.surname)

        //itemView.findViewById<TextView>(R.id.textViewAddress).setText(student.currentJobCity + ", "  + graduate.currentJobCountry)
        //itemView.findViewById<TextView>(R.id.textViewProgram).setText(student.programName)
        // Picasso.get().load(graduate.profilePhotoLink).into(itemView.findViewById<ImageView>(R.id.imageView))

        itemView.setOnClickListener{
            val intent = Intent(itemView.context, LoginActivity::class.java)
            intent.putExtra("uid", student.uid)
            itemView.context.startActivity(intent)
        }
    }
}