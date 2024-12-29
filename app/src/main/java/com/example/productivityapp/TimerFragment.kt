package com.example.productivityapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {
    private lateinit var timerTextView: TextView
    private lateinit var startStopButton: Button
    private lateinit var resetButton: Button
    private var isRunning = false
    private var seconds = 0
    private lateinit var handler: Handler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        timerTextView = view.findViewById(R.id.timerTextView)
        startStopButton = view.findViewById(R.id.startStopButton)
        resetButton = view.findViewById(R.id.resetButton)

        handler = Handler(Looper.getMainLooper())

        startStopButton.setOnClickListener {
            if (isRunning) {
                stopTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }

        return view
    }

    private fun startTimer() {
        isRunning = true
        startStopButton.text = "Stop"
        handler.post(object : Runnable {
            override fun run() {
                if (isRunning) {
                    seconds++
                    updateTimerText()
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun stopTimer() {
        isRunning = false
        startStopButton.text = "Start"
    }

    private fun resetTimer() {
        stopTimer()
        seconds = 0
        updateTimerText()
    }

    private fun updateTimerText() {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}