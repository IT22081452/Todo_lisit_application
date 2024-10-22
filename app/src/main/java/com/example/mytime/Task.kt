package com.example.mytime

data class Task(
    var title: String,
    var description: String,
    var date: String,
    var time: String,
    var remainingTime: Long
)
