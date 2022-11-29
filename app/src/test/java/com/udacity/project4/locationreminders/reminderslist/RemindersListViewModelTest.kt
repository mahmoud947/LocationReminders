package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private var remindersList = mutableListOf<ReminderDTO>()


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    var testContext: Context = InstrumentationRegistry.getInstrumentation().context


    @Before
    fun initViewModel() {
        stopKoin()
        dataSource = FakeDataSource(remindersList)
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun cleanup() = runBlocking {
        dataSource.deleteAllReminders()
    }

    @Test
    fun check_loading(){

        //When
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()

        //Then show loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }



    @Test
    fun loadReminders_dataSourceWithData_both_showNoDataIsFalse_and_remindersListIsNotEmptyOrNull() {

        //GIVEN datasource with no empty reminders
        val reminders = listOf(
            ReminderDTO(
                "Title1",
                "Description1",
                location = "location1",
                latitude = 222.222,
                longitude = 222.22
            ),
            ReminderDTO(
                "Title2",
                "Description2",
                location = "location2",
                latitude = 222.222,
                longitude = 222.22
            ),
            ReminderDTO(
                "Title3",
                "Description3",
                location = "location3",
                latitude = 222.222,
                longitude = 222.22
            ),
            ReminderDTO(
                "Title4",
                "Description4",
                location = "location4",
                latitude = 222.222,
                longitude = 222.22
            ),
            ReminderDTO(
                "Title5",
                "Description5",
                location = "location5",
                latitude = 222.222,
                longitude = 222.22
            )
        )
        dataSource.addReminders(reminder = reminders)

        mainCoroutineRule.pauseDispatcher()
        //When
        viewModel.loadReminders()

        //THEN  showLoading is  true in first time and should be false after successfully loading data
        //      reminderList is not empty

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.remindersList.getOrAwaitValue(), notNullValue())
        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(reminders.toItem()))

    }


    @Test
    fun loadReminders_dataSourceEmptyData_both_showNoDataIsTrue_and_remindersListIsNotEmptyOrNull() =
        mainCoroutineRule.runBlockingTest {

            //GIVEN datasource with no empty reminders
            val reminders = listOf<ReminderDTO>(
                ReminderDTO(
                    "Title1",
                    "Description1",
                    location = "location1",
                    latitude = 222.222,
                    longitude = 222.22
                ),
                ReminderDTO(
                    "Title4",
                    "Description4",
                    location = "location4",
                    latitude = 222.222,
                    longitude = 222.22
                ),
                ReminderDTO(
                    "Title5",
                    "Description5",
                    location = "location5",
                    latitude = 222.222,
                    longitude = 222.22
                )
            )
            dataSource.addReminders(reminder = reminders)
            // remove all data to make reminder list empty
            dataSource.deleteAllReminders()

            mainCoroutineRule.pauseDispatcher()
            //When
            viewModel.loadReminders()

            //THEN  showLoading is  true in first time and should be false after successfully loading data
            //      reminderList is empty
            //      showNoData is true

            assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

            mainCoroutineRule.resumeDispatcher()
            assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
            assertThat(viewModel.remindersList.getOrAwaitValue(), notNullValue())
            assertThat(viewModel.remindersList.getOrAwaitValue().size, `is`(0))
            assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))

        }


    @Test
    fun loadReminders_dataSourceWithNullData_showSnackBarWithErrorMessage() {

        //GIVEN make datasource return error
        dataSource.setIsReturnError(true)

        mainCoroutineRule.pauseDispatcher()
        //When
        viewModel.loadReminders()

        //THEN  showLoading is  true in first time and should be false after successfully loading data
        //      reminderList is empty
        //      show showSnackBar with error message

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Test error message"))
    }


    fun List<ReminderDTO>.toItem(): List<ReminderDataItem> {
        return this.map {
            ReminderDataItem(
                it.title,
                it.description,
                it.location,
                it.latitude,
                it.longitude,
                it.id
            )
        }
    }

}