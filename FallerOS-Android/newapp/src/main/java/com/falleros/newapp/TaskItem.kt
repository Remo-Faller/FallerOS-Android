package com.falleros.newapp

data class TaskItem(
    val title: String,
    val description: String,
    val category: String,
    val progress: Int,
    val timeLabel: String
)
