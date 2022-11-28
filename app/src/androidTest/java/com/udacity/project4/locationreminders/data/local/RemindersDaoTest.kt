package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt


    @get:Rule
    var instanceTaskException = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveReminderAndGetItById() = runBlockingTest {

        //GIVEN save reminder in database
        val reminderDTO = ReminderDTO(
            "Title",
            "description",
            location = "Location",
            longitude = 222.222,
            latitude = 222.22
        )
        database.reminderDao().saveReminder(reminderDTO)

        //WHEN get reminder by it's id from database
        val result=database.reminderDao().getReminderById(reminderDTO.id)

        //THEN the reminder result content the same data that saved
        assertThat(result as ReminderDTO, notNullValue())
        assertThat(result.id,`is`(reminderDTO.id))
        assertThat(result.title,`is`(reminderDTO.title))
        assertThat(result.description,`is`(reminderDTO.description))
        assertThat(result.location,`is`(reminderDTO.location))
        assertThat(result.longitude,`is`(reminderDTO.longitude))
        assertThat(result.latitude,`is`(reminderDTO.latitude))
    }

    @Test
    fun getReminders_withData_returnListOfReminderDTO()= runBlockingTest{
        //GIVEN save reminder in database
        val reminderDTO = ReminderDTO(
            "Title",
            "description",
            location = "Location",
            longitude = 222.222,
            latitude = 222.22
        )
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN get reminders
        val result = database.reminderDao().getReminders()

        // THEN the Reminders should be not emptyList
        assertThat(result as List<ReminderDTO>, notNullValue())
        assertThat(result.size,greaterThan(0) )
    }

    @Test
    fun getReminders_withNoData_returnEmptyListOfReminderDTO()= runBlockingTest{

        // WHEN get reminders
        val result = database.reminderDao().getReminders()

        // THEN the Reminders should be not emptyList
        assertThat(result as List<ReminderDTO>, notNullValue())
        assertThat(result.size,`is`(0) )
    }

    @Test
    fun deleteAllReminders_returnEmptyListOfReminderDTO()= runBlockingTest{
        //GIVEN save reminder in database
        val reminderDTO = ReminderDTO(
            "Title",
            "description",
            location = "Location",
            longitude = 222.222,
            latitude = 222.22
        )
        database.reminderDao().saveReminder(reminderDTO)
        // WHEN delete all  reminders
        database.reminderDao().deleteAllReminders()
        val result = database.reminderDao().getReminders()

        // THEN the result of getAllReminders should be emptyList
        assertThat(result as List<ReminderDTO>, notNullValue())
        assertThat(result.size,`is`(0) )
    }




}