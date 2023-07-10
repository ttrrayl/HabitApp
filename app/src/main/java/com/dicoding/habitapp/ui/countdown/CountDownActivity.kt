package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.*
import org.w3c.dom.Text

class CountDownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this) {
            findViewById<TextView>(R.id.tv_count_down).text = it
        }
        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
        val data = Data.Builder()
            .putInt(HABIT_ID, habit.id)
            .putString(HABIT_TITLE, habit.title)
            .putString(NOTIFICATION_CHANNEL_ID, getString(R.string.notify_channel_name))
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(data)
            .addTag(NOTIF_UNIQUE_WORK)
            .build()

        viewModel.eventCountDownFinish.observe(this) {
            if (it) {
                WorkManager.getInstance(this).enqueueUniqueWork(
                    NOTIF_UNIQUE_WORK,
                    ExistingWorkPolicy.REPLACE,
                    oneTimeWorkRequest
                )
                updateButtonState(false)
            }

        }

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            updateButtonState(true)
            viewModel.startTimer()
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            updateButtonState(false)
            WorkManager.getInstance(this).cancelUniqueWork(NOTIF_UNIQUE_WORK)
        }

    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}