package com.falleros.newapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val items: List<TaskItem>
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val category: TextView = itemView.findViewById(R.id.taskCategory)
        val description: TextView = itemView.findViewById(R.id.taskDescription)
        val timeLabel: TextView = itemView.findViewById(R.id.taskTime)
        val progressBar: ProgressBar = itemView.findViewById(R.id.taskProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.category.text = item.category
        holder.description.text = item.description
        holder.timeLabel.text = item.timeLabel
        holder.progressBar.progress = item.progress
    }

    override fun getItemCount(): Int = items.size
}
