package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {
    private var isReturnError = false
    fun setIsReturnError(value: Boolean) {
        isReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (isReturnError)
            return Result.Error("Test error message")

            return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (isReturnError)
            return Result.Error("Test error message")
        val reminder = reminders.firstOrNull {
            it.id == id
        }
        reminder?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    fun addReminders(reminder: List<ReminderDTO>) {
        reminders.addAll(reminder)
    }

}