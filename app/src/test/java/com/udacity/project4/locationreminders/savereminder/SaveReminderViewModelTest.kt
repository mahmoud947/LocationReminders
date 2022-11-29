package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource
    private var remindersList = mutableListOf<ReminderDTO>()


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    var testContext: Context = getInstrumentation().getContext()


    //TODO: provide testing to the SaveReminderView and its live data objects
    @Before
    fun initViewModel() {
        stopKoin()
        dataSource = FakeDataSource(remindersList)
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }


    @Test
    fun check_loading(){
        //GIVEN add new valid reminder
        val reminder = ReminderDataItem(
            title = "Title",
            description = "description",
            location = "location",
            longitude = 222.22,
            latitude = 222.22
        )
        mainCoroutineRule.pauseDispatcher()


        // WHEN saveReminder
        viewModel.validateAndSaveReminder(reminder)

        //THEN  showToast(Reminder Saved !)
        //      showLoading is  true in first time and should be false after successfully saved
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateAndSaveReminder_validData_showSavedToast() {
        //GIVEN add new valid reminder
        val reminder = ReminderDataItem(
            title = "Title",
            description = "description",
            location = "location",
            longitude = 222.22,
            latitude = 222.22
        )
        mainCoroutineRule.pauseDispatcher()


        // WHEN saveReminder
        viewModel.validateAndSaveReminder(reminder)

        //THEN  showToast(Reminder Saved !)
        //      showLoading is  true in first time and should be false after successfully saved
        //      navigationCommand is NavigationCommand.Back
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.showToast.getOrAwaitValue(), notNullValue())
        assertThat(
            viewModel.showToast.getOrAwaitValue(),
            `is`(testContext.getString(R.string.reminder_saved))
        )
        Assert.assertTrue(viewModel.navigationCommand.value is NavigationCommand.Back)

    }


    @Test
    fun validateAndSaveReminder_invalidDataTitleIsNullOrEmpty_showSnackBar() {
        //GIVEN add new valid reminder
        val reminder = ReminderDataItem(
            title = null,
            description = "description",
            location = null,
            longitude = null,
            latitude = null
        )
        mainCoroutineRule.pauseDispatcher()


        // WHEN validateAndSaveReminder
        viewModel.validateAndSaveReminder(reminder)

        //THEN show snackbar (please enter title)
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )

    }

    @Test
    fun validateAndSaveReminder_invalidDataLocationIsNullOrEmpty_showSnackBar() {
        //GIVEN add new valid reminder
        val reminder = ReminderDataItem(
            title = "Title",
            description = "description",
            location = null,
            longitude = null,
            latitude = null
        )
        mainCoroutineRule.pauseDispatcher()


        // WHEN validateAndSaveReminder
        viewModel.validateAndSaveReminder(reminder)

        //THEN show snackbar (please select location)
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )

    }


    @Test
    fun validateEnteredData_ValidData_returnTrue() {
        //GIVEN add new valid reminder
        val reminder = ReminderDataItem(
            title = "Title",
            description = "description",
            location = "location",
            longitude = 222.22,
            latitude = 222.22
        )

        // WHEN
        val result = viewModel.validateEnteredData(reminder)

        //THEN  return true if the data entered is valid data
        assertThat(result,`is`(true))
    }


    @Test
    fun validateEnteredData_invalidDataTitleIsNullOrEmpty_both_showSnackBar_returnFalse() {
        //GIVEN add new valid reminder
        val reminder = ReminderDataItem(
            title = null,
            description = "description",
            location = null,
            longitude = null,
            latitude = null
        )

        // WHEN
        val result = viewModel.validateEnteredData(reminder)

        //THEN  return true if the data entered is valid data
        assertThat(result,`is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
    }



    @Test
    fun validateEnteredData_invalidDataLocationIsNullOrEmpty_both_showSnackBar_returnFalse() {
        //GIVEN add new valid reminder
        val reminder = ReminderDataItem(
            title = "Title",
            description = "description",
            location = null,
            longitude = null,
            latitude = null
        )

        // WHEN
        val result = viewModel.validateEnteredData(reminder)

        //THEN  return true if the data entered is valid data
        assertThat(result,`is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

}