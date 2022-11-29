package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ReminderDataSource

    private fun initTestModel() {
        stopKoin()
        val testModel = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin { modules(testModel) }
    }


    @Before
    fun setUp() {
        initTestModel()
        repository = GlobalContext.get().koin.get()
        runBlocking { repository.deleteAllReminders() }
    }

    @After
    fun cleanUp() {
        // runBlocking { repository.deleteAllReminders() }
        stopKoin()
    }

//    TODO: test the navigation of the fragments.

    @Test
    fun testNavigationToSaveReminderFragmentOnClickOnAddReminderFAB() {
        //GIVEn on the Location Reminders Fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        // WHEN click on the add button(+)
        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    //    TODO: test the displayed data on the UI.

    @Test
    fun remindersDetails_DisplayedInUi() {
        //GIVEN add reminder to database
        val reminders = listOf(
            ReminderDTO("Title1", "Description1", "Location1", 222.222, 222.22),
            ReminderDTO("Title2", "Description2", "Location2", 222.222, 222.22),
            ReminderDTO("Title3", "Description3", "Location3", 222.222, 222.22)
        )
        runBlocking {
            reminders.forEach {
                repository.saveReminder(it)
            }
        }
        //WHEN reminderListFragment launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //THEN reminder details are displayed on the screen
        onView(withText("Title1")).check(matches(isDisplayed()))
        onView(withText("Description1")).check(matches(isDisplayed()))
        onView(withText("Location1")).check(matches(isDisplayed()))

        onView(withText("Title2")).check(matches(isDisplayed()))
        onView(withText("Description2")).check(matches(isDisplayed()))
        onView(withText("Location2")).check(matches(isDisplayed()))

        onView(withText("Title3")).check(matches(isDisplayed()))
        onView(withText("Description3")).check(matches(isDisplayed()))
        onView(withText("Location3")).check(matches(isDisplayed()))
    }


    // TODO: add testing for the error messages.
    @Test
    fun remindersDetails_EmptyReminders_DisplayedErrorMessage(){
        //WHEN reminderListFragment launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN noDataTextView shoe displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
}