package com.example.productivityapp

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedViewModel(application: Application) : ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasks

    private val _reminders = MutableLiveData<MutableList<Reminder>>(mutableListOf())
    val reminders: LiveData<MutableList<Reminder>> = _reminders

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("TaskPrefs", Application.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadTasksFromPreferences()
    }

    private fun loadTasksFromPreferences() {
        val tasksJson = sharedPreferences.getString("tasks", "[]")
        val type = object : TypeToken<List<Task>>() {}.type
        _tasks.value = gson.fromJson(tasksJson, type)
    }

    private fun saveTasksToPreferences() {
        val tasksJson = gson.toJson(_tasks.value)
        sharedPreferences.edit().putString("tasks", tasksJson).apply()
    }

    fun addTask(task: Task) {
        val currentTasks = _tasks.value.orEmpty().toMutableList()
        currentTasks.add(task)
        _tasks.value = currentTasks
        saveTasksToPreferences()
        Log.d("SharedViewModel", "Task added: ${task.title}. Total tasks: ${currentTasks.size}")
    }

    fun updateTask(updatedTask: Task) {
        val currentTasks = _tasks.value.orEmpty().toMutableList()
        val index = currentTasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            currentTasks[index] = updatedTask
            _tasks.value = currentTasks
            saveTasksToPreferences()
            Log.d("SharedViewModel", "Task updated: ${updatedTask.title}")
        }
    }

    fun removeTask(task: Task) {
        val currentTasks = _tasks.value.orEmpty().toMutableList()
        if (currentTasks.remove(task)) {
            _tasks.value = currentTasks
            saveTasksToPreferences()
            Log.d("SharedViewModel", "Task removed: ${task.title}. Remaining tasks: ${currentTasks.size}")
        }
    }

    fun addReminder(reminder: Reminder) {
        val currentReminders = _reminders.value ?: mutableListOf()
        currentReminders.add(reminder)
        _reminders.value = currentReminders
    }

    fun removeReminder(reminder: Reminder) {
        _reminders.value?.remove(reminder)
        _reminders.value = _reminders.value // Trigger LiveData update
    }
}
