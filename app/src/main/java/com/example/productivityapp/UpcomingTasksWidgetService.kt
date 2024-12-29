package com.example.productivityapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class UpcomingTasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return UpcomingTasksRemoteViewsFactory(this.applicationContext)
    }
}

class UpcomingTasksRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var tasks: List<Task> = listOf()
    private val gson = Gson()

    override fun onCreate() {
        Log.d("RemoteViewsFactory", "onCreate called")
    }

    override fun onDataSetChanged() {
        Log.d("RemoteViewsFactory", "onDataSetChanged called")
        loadTasksFromPreferences()
    }

    private fun loadTasksFromPreferences() {
        val sharedPreferences = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString("tasks", "[]")
        val type = object : TypeToken<List<Task>>() {}.type
        tasks = gson.fromJson(tasksJson, type)
        Log.d("RemoteViewsFactory", "Loaded ${tasks.size} tasks from preferences")
    }

    override fun onDestroy() {
        // Clean up if necessary
    }

    override fun getCount(): Int {
        Log.d("RemoteViewsFactory", "getCount called. Returning ${tasks.size}")
        return tasks.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        Log.d("RemoteViewsFactory", "getViewAt called for position: $position")
        if (position < 0 || position >= tasks.size) {
            return RemoteViews(context.packageName, R.layout.widget_task_item)
        }
        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)
        views.setTextViewText(R.id.widget_task_title, task.title)
        views.setTextViewText(R.id.widget_task_status, if (task.isCompleted) "Completed" else "Pending")
        Log.d("RemoteViewsFactory", "Returning view for task: ${task.title}")
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}