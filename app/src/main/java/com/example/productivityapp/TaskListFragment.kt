package com.example.productivityapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class TaskListFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        taskAdapter = TaskAdapter(
            mutableListOf(),
            { task -> sharedViewModel.updateTask(task) },
            { task -> showDeleteTaskDialog(task) },
            { task -> showEditTaskDialog(task) }
        )

        val recyclerView: RecyclerView = view.findViewById(R.id.taskRecyclerView)
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        sharedViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.updateTasks(tasks)
        }

        val addButton: FloatingActionButton = view.findViewById(R.id.addTaskFab)
        addButton.setOnClickListener {
            showAddTaskDialog()
        }

        return view
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskInput: EditText = dialogView.findViewById(R.id.taskTitleInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val taskText = taskInput.text.toString()
                if (taskText.isNotEmpty()) {
                    sharedViewModel.addTask(Task(title = taskText, isCompleted = false))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskInput: EditText = dialogView.findViewById(R.id.taskTitleInput)
        taskInput.setText(task.title)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedTaskText = taskInput.text.toString()
                if (updatedTaskText.isNotEmpty()) {
                    sharedViewModel.updateTask(task.copy(title = updatedTaskText))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteTaskDialog(task: Task) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                sharedViewModel.removeTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}