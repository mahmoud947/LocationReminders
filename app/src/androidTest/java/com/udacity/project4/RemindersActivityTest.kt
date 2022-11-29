package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var decorView: View

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    //    TODO: add End to End testing to the app
    @Test
    fun addNewReminder_and_showReminderInRecyclerView_and_toast() {
        //GIVEN start the activity
        val reminderActivityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario = reminderActivityScenario)
        reminderActivityScenario.onActivity { decorView = it.window.decorView }
        //WHEN
        // click on fab to add new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        // check if reminder title,description,selectLocation is displayed
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        // click select location to select the location of reminder
        onView(withId(R.id.selectLocation)).perform(click())

        // check if saveButton is Displayed
        onView(withId(R.id.saveBtn)).check(matches(isDisplayed()))
        //click on saveButton to confirm the location
        onView(withId(R.id.saveBtn)).perform(click())

        // add title and description to the reminder
        onView(withId(R.id.reminderTitle)).perform(replaceText("Title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description"))

        //click on fab(saveReminder)
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN
        //check if reminder that is inserted is displayed on recyclerView and toast message is displayed
        onView(withText("Title")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(decorView))).check(
            matches(isDisplayed())
        )
    }


    @Test
    fun addNewReminder_withOut_title_show_snackBar() {
        //GIVEN start the activity
        val reminderActivityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario = reminderActivityScenario)
        //WHEN
        //click on fab to add new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        // check if reminder title,description,selectLocation is displayed
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        // click select location to select the location of reminder
        onView(withId(R.id.selectLocation)).perform(click())

        // check if saveButton is Displayed
        onView(withId(R.id.saveBtn)).check(matches(isDisplayed()))
        //click on saveButton to confirm the location
        onView(withId(R.id.saveBtn)).perform(click())

        // add only description to the reminder
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description"))

        //click on fab(saveReminder)
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN the snackBar with error message should display
        onView(withId(R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(R.id.snackbar_text)).check(matches(withText("Please enter title")))
    }


    @Test
    fun addNewReminder_Location_Description_show_snackBar() {
        //GIVEN start the activity
        val reminderActivityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario = reminderActivityScenario)
        //WHEN
        //click on fab to add new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        // check if reminder title,description,selectLocation is displayed
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))


        // add title and description to the reminder with out select location
        onView(withId(R.id.reminderTitle)).perform(replaceText("Title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description"))

        //click on fab(saveReminder)
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN the snackBar with error message should display
        onView(withId(R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(R.id.snackbar_text)).check(matches(withText("Please select location")))
    }
}
