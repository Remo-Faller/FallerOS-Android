package com.falleros.newapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TaskAdapter(
            listOf(
                TaskItem(
                    title = "Design sprint",
                    description = "Finalize mobile onboarding flow",
                    category = "Product",
                    progress = 78,
                    timeLabel = "2h 15m left"
                ),
                TaskItem(
                    title = "Client review",
                    description = "Prepare milestone summary for stakeholders",
                    category = "Meeting",
                    progress = 52,
                    timeLabel = "1h 10m left"
                ),
                TaskItem(
                    title = "Code cleanup",
                    description = "Refactor analytics and notifications",
                    category = "Development",
                    progress = 91,
                    timeLabel = "45m left"
                ),
                TaskItem(
                    title = "Wellness break",
                    description = "Stretch, hydrate, and reset focus",
                    category = "Personal",
                    progress = 34,
                    timeLabel = "15m"
                )
            )
        )
    }
}
