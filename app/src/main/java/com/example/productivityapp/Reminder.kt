package com.example.productivityapp

data class Reminder(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val time: String
)
