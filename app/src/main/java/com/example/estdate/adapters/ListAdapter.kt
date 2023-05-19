package com.example.estdate.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.estdate.R
import com.example.estdate.models.Student
import com.example.estdate.viewHolders.ListItemViewHolder

class ListAdapter(var list: MutableList<Student>): RecyclerView.Adapter<ListItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        return ListItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    fun setData(newData: MutableList<Student>) {
        list = newData
    }

}