package com.example.mytime

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context, private val tasks: List<Task>) :
    ArrayAdapter<Task>(context, 0, tasks) {

    private val activeTimers = mutableMapOf<Int, CountDownTimer>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        // Find views in the custom layout
        val taskTitle = view.findViewById<TextView>(R.id.taskitle)
        val taskDescription = view.findViewById<TextView>(R.id.taskDescription)
        val taskDateTime = view.findViewById<TextView>(R.id.taskdateTime)
        val taskCountdown = view.findViewById<TextView>(R.id.taskCountdown)

        // Set the values from the Task object
        taskTitle.text = task?.title
        taskDescription.text = task?.description
        taskDateTime.text = "${task?.date} at ${task?.time}"

        // Stop any previous countdown timers for this view
        activeTimers[position]?.cancel()

        // Handle the countdown
        task?.let {
            val remainingTime = calculateRemainingTime(it.date, it.time)
            startCountdown(taskCountdown, remainingTime, position, task?.title ?: "Task")
        }

        return view
    }

    // Function to start the countdown
    private fun startCountdown(countdownView: TextView, remainingTime: Long, position: Int, taskTitle: String) {
        if (remainingTime > 0) {
            val timer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1000 * 60 * 60) % 24
                    val minutes = millisUntilFinished / (1000 * 60) % 60
                    val seconds = millisUntilFinished / 1000 % 60
                    countdownView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    countdownView.text = "Task time over!"
                    showAlert(taskTitle)
                }
            }.start()

            // Store the active timer for this task's position
            activeTimers[position] = timer
        } else {
            countdownView.text = "Task time over!"
        }
    }

    // Helper function to calculate remaining time
    private fun calculateRemainingTime(date: String, time: String): Long {
        val taskDateTime = "$date $time"
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val taskTime = format.parse(taskDateTime)
        return taskTime?.time?.minus(System.currentTimeMillis()) ?: 0
    }

    // Function to show the alert dialog when the countdown finishes
    private fun showAlert(taskTitle: String) {
        // Play ringtone
        playRingtone()

        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Task Alert")
            .setMessage("The countdown for '$taskTitle' is over!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    // Function to play a ringtone when the countdown finishes
    private fun playRingtone() {
        val alertSound: Uri = Uri.parse("android.resource://${context.packageName}/raw/atria")
        val mediaPlayer = MediaPlayer.create(context, alertSound)
        mediaPlayer.start()
    }
}
