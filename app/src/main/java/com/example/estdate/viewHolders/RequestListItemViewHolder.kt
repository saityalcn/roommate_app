package com.example.estdate.viewHolders

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.estdate.ProfileActivity
import com.example.estdate.R
import com.example.estdate.models.Student

class RequestListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindItems(student: Student){
        itemView.findViewById<TextView>(R.id.textViewGraduteName).setText(student.name + " " + student.surname)

        Glide.with(itemView)
            .load(Uri.parse(student.profileImageUrl))
            .into(itemView.findViewById<ImageView>(R.id.imageView))

        itemView.findViewById<TextView>(R.id.textViewAddress).text = student.address
        itemView.findViewById<TextView>(R.id.textViewState).text = student.state

        itemView.setOnClickListener{
            val intent = Intent(itemView.context, ProfileActivity::class.java)
            intent.putExtra("uid", student.uid)
            itemView.context.startActivity(intent)
        }
    }
}