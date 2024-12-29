package com.example.productivityapp

import android.Manifest
import android.app.TimePickerDialog
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import androidx.lifecycle.ViewModelProvider

class ReminderFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var alarmManager: AlarmManager
    private var pendingReminder: Reminder? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pendingReminder?.let { scheduleNotification(it) }
            pendingReminder = null
        } else {
            // Inform the user of the importance of granting permission
            Toast.makeText(context, "Permission denied. Exact alarms cannot be scheduled.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_reminders, container, false)
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        reminderAdapter = ReminderAdapter(mutableListOf()) { reminder ->
            sharedViewModel.removeReminder(reminder)
        }

        // Setup recycler view to display reminders
        val recyclerView: RecyclerView = view.findViewById(R.id.reminderRecyclerView)
        recyclerView.adapter = reminderAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Observe reminder data to update UI when it changes
        sharedViewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            reminderAdapter.updateReminders(reminders)
        }

        // Add reminder button functionality
        val addButton: FloatingActionButton = view.findViewById(R.id.addReminderFab)
        addButton.setOnClickListener {
            showAddReminderDialog()
        }

        // Show whether the app has the necessary permissions for exact alarms (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
            Log.d("ReminderFragment", "Set a Reminder: $canScheduleExactAlarms")
            if (canScheduleExactAlarms) {
                Toast.makeText(context, "You can set Reminders", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Exact alarms cannot be set. Permission required.", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    // Function to show the dialog for adding a new reminder
    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val titleInput: EditText = dialogView.findViewById(R.id.reminderTitleInput)
        val timeInput: EditText = dialogView.findViewById(R.id.reminderTimeInput)

        // Open time picker when time input is clicked
        timeInput.setOnClickListener {
            showTimePicker { selectedTime ->
                timeInput.setText(selectedTime)
            }
        }

        // Build and show the reminder dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Reminder")
            .setView(dialogView)
            .setPositiveButton("Add", null) // Listener added later to avoid auto-dismiss
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val title = titleInput.text.toString()
                val time = timeInput.text.toString()

                // Validate title and time
                if (title.isNotEmpty() && isValidTime(time)) {
                    val newReminder = Reminder(
                        id = System.currentTimeMillis(),
                        title = title,
                        time = time
                    )
                    addReminder(newReminder)
                    dialog.dismiss()
                } else {
                    if (title.isEmpty()) {
                        titleInput.error = "Title cannot be empty"
                    }
                    if (!isValidTime(time)) {
                        timeInput.error = "Invalid time format (HH:MM required)"
                    }
                }
            }
        }

        dialog.show()
    }

    private fun addReminder(reminder: Reminder) {
        sharedViewModel.addReminder(reminder)

        // Schedule notification or ask for permission if necessary (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleNotification(reminder)
            } else {
                pendingReminder = reminder
                requestScheduleExactAlarmPermission()
            }
        } else {
            scheduleNotification(reminder)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(reminder: Reminder) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        intent.putExtra("REMINDER_TITLE", reminder.title)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeInMillis = parseTimeToMillis(reminder.time)

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            Toast.makeText(context, "Reminder set for ${reminder.time}", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, "Unable to schedule reminder. Grant permission in settings.", Toast.LENGTH_LONG).show()
            navigateToAlarmSettings()
        }
    }

    // Parse time string to milliseconds
    private fun parseTimeToMillis(time: String): Long {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        return calendar.timeInMillis
    }

    // Request permission for exact alarms (Android 12+)
    private fun requestScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
        }
    }

    // Navigate to settings for granting alarm permission
    private fun navigateToAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
        }
    }

    // Helper function to show time picker dialog
    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true).show()
    }

    // Validate time format (HH:MM)
    private fun isValidTime(time: String): Boolean {
        return time.matches(Regex("\\d{2}:\\d{2}"))
    }
}
