package com.example.estdate.adapters

import android.app.LauncherActivity.ListItem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.estdate.R
import com.example.estdate.models.Student
import com.example.estdate.viewHolders.ListItemViewHolder
import com.example.estdate.viewHolders.RequestListItemViewHolder

class RequestsListAdapter(var list: MutableList<Student>): RecyclerView.Adapter<RequestListItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestListItemViewHolder {
        return RequestListItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.requests_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RequestListItemViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    fun setData(newData: MutableList<Student>) {
        list = newData
    }

}