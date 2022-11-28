package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource (private var reminders:MutableList<ReminderDTO>? = mutableListOf()): ReminderDataSource {


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
       reminders?.let {
           return Result.Success(it.toList())
       }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
       reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
      val reminder= reminders?.firstOrNull {
          it.id==id }
        reminder?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

}