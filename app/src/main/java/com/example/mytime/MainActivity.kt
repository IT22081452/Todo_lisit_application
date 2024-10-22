package com.example.mytime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var taskInput: EditText
    lateinit var taskDescription: EditText
    lateinit var btnPickDate: Button
    lateinit var btnPickTime: Button
    lateinit var btnAddUpdate: Button
    lateinit var taskListView: ListView
    lateinit var taskAdapter: TaskAdapter
    var taskList = mutableListOf<Task>()
    var selectedTaskIndex: Int? = null
    var selectedDate: String = ""
    var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskInput = findViewById(R.id.taskInput)
        taskDescription = findViewById(R.id.taskDescription)
        btnPickDate = findViewById(R.id.btnPickDate)
        btnPickTime = findViewById(R.id.btnPickTime)
        btnAddUpdate = findViewById(R.id.btnAddUpdate)
        taskListView = findViewById(R.id.taskList)

        // Load tasks from SharedPreferences
        loadTasks()

        // Set up the custom adapter for the ListView
        taskAdapter = TaskAdapter(this, taskList)
        taskListView.adapter = taskAdapter

        // Date Picker
        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedDate = "$dayOfMonth/${month + 1}/$year"
                btnPickDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        // Time Picker
        btnPickTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedTime = "$hourOfDay:$minute"
                btnPickTime.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }

        // Add/Update task button click listener
        btnAddUpdate.setOnClickListener {
            val taskTitle = taskInput.text.toString()
            val taskDesc = taskDescription.text.toString()

            if (taskTitle.isNotEmpty() && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                val remainingTime = calculateRemainingTime(selectedDate, selectedTime)

                if (selectedTaskIndex == null) {
                    // Add new task
                    taskList.add(Task(taskTitle, taskDesc, selectedDate, selectedTime, remainingTime))
                } else {
                    // Update existing task
                    taskList[selectedTaskIndex!!] = Task(taskTitle, taskDesc, selectedDate, selectedTime, remainingTime)
                    selectedTaskIndex = null
                    btnAddUpdate.text = "Add Task"
                }

                // Save tasks to SharedPreferences
                saveTasks()
                clearInputs()
                taskAdapter.notifyDataSetChanged()
            }
        }

        // Handle task selection for editing
        taskListView.setOnItemClickListener { _, _, position, _ ->
            val selectedTask = taskList[position]
            taskInput.setText(selectedTask.title)
            taskDescription.setText(selectedTask.description)
            selectedDate = selectedTask.date
            selectedTime = selectedTask.time
            btnPickDate.text = selectedDate
            btnPickTime.text = selectedTime
            selectedTaskIndex = position
            btnAddUpdate.text = "Update Task"
        }

        // Handle task long press for deletion
        taskListView.setOnItemLongClickListener { _, _, position, _ ->
            taskList.removeAt(position)
            saveTasks()
            taskAdapter.notifyDataSetChanged()
            true
        }
    }

    // Function to calculate the remaining time until the task's date and time
    private fun calculateRemainingTime(date: String, time: String): Long {
        val taskDateTime = "$date $time"
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val taskTime = format.parse(taskDateTime)
        return taskTime?.time?.minus(System.currentTimeMillis()) ?: 0
    }

    // Save tasks to SharedPreferences
    private fun saveTasks() {
        val sharedPref = getSharedPreferences("MyTimeTasks", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("tasks", taskList.joinToString(";") { "${it.title},${it.description},${it.date},${it.time},${it.remainingTime}" })
        editor.apply()
    }

    // Load tasks from SharedPreferences
    private fun loadTasks() {
        val sharedPref = getSharedPreferences("MyTimeTasks", Context.MODE_PRIVATE)
        val savedTasks = sharedPref.getString("tasks", "")
        if (savedTasks != null && savedTasks.isNotEmpty()) {
            taskList.addAll(savedTasks.split(";").map {
                val taskDetails = it.split(",")
                Task(taskDetails[0], taskDetails[1], taskDetails[2], taskDetails[3], calculateRemainingTime(taskDetails[2], taskDetails[3]))
            })
        }
    }

    // Clear input fields after saving/updating
    private fun clearInputs() {
        taskInput.text.clear()
        taskDescription.text.clear()
        selectedDate = ""
        selectedTime = ""
        btnPickDate.text = "Pick Date"
        btnPickTime.text = "Pick Time"
    }
}
