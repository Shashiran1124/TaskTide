package com.example.productivityapp

import java.io.Serializable

data class Task(
    val id: Long = System.currentTimeMillis(), // Use timestamp as a simple unique id
    val title: String,
    var isCompleted: Boolean = false
) : Serializable