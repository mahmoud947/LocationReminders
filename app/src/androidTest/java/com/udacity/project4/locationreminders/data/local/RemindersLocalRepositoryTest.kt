package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.dto.Result
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    //    TODO: Add testing implementation to the RemindersLocalRepository.kt
    @get:Rule
    var instanceTaskException = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    private fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()
    }


    @Before
    fun setupRepository(){
        initDatabase()
        repository = RemindersLocalRepository(database.reminderDao(),Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getReminders_retrieveNotNullOrEmptyRemindersList()= runBlocking{
        //GIVEN Add reminders to database
        val reminders= listOf(
            ReminderDTO(
                "Title1",
                "description1",
                location = "Location1",
                longitude = 111.111,
                latitude = 111.11
            ),
            ReminderDTO(
                "Title",
                "description",
                location = "Location",
                longitude = 222.222,
                latitude = 222.22
            )
        )
        reminders.forEach {
            repository.saveReminder(it)
        }

        // WHEN call getReminders
        val result=repository.getReminders() as Result.Success

        //Then
        //  result not null or empty
        assertThat(result.data, notNullValue())
        assertThat(result.data.size, greaterThan(0))
    }

    @Test
    fun getReminder_ReminderNotFound_returnErrorMessage()= runBlocking{
        //GIVEN
        val reminder=ReminderDTO(
            "Title1",
            "description1",
            location = "Location1",
            longitude = 111.111,
            latitude = 111.11
        )

        //WHEN get reminder by id the result should be Error
        val result = repository.getReminder(reminder.id) as Result.Error

        // Then
        assertThat(result.message,`is`("Reminder not found!"))
    }

    @Test
    fun saveReminder_and_retrieveIt()= runBlocking{

        //GIVEN Add reminder to database
        val reminder=ReminderDTO(
            "Title1",
            "description1",
            location = "Location1",
            longitude = 111.111,
            latitude = 111.11
        )
        repository.saveReminder(reminder)

        //WHEN get reminder by id the result should be same as inserted reminder
        val result = repository.getReminder(reminder.id) as Result.Success

        // Then

        assertThat(result.data.id,`is`(reminder.id))
        assertThat(result.data.title,`is`(reminder.title))
        assertThat(result.data.description,`is`(reminder.description))
        assertThat(result.data.latitude,`is`(reminder.latitude))
        assertThat(result.data.longitude,`is`(reminder.longitude))
        assertThat(result.data.location,`is`(reminder.location))
    }

    @Test
    fun deleteAllReminders_retrieve_emptyListOfReminders()= runBlocking{

        //GIVEN Add reminder to database
        val reminder=ReminderDTO(
            "Title1",
            "description1",
            location = "Location1",
            longitude = 111.111,
            latitude = 111.11
        )
        repository.saveReminder(reminder)

        //WHEN delete all reminders
        repository.deleteAllReminders()

        // Then reminder list should be empty and not null
        val result = repository.getReminders() as Result.Success

        assertThat(result.data, notNullValue())
        assertThat(result.data.size, `is`(0))
    }



}