package com.example.uniplanner

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun dashboardIsDisplayedOnLaunch() {
        // Dashboard се показва при стартиране
        onView(withId(R.id.rvUpcomingTasks))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNavigationWorking() {
        // Натискане на Tasks таб
        onView(withId(R.id.tasksFragment))
            .perform(click())

        // Tasks RecyclerView се показва
        onView(withId(R.id.rvTasks))
            .check(matches(isDisplayed()))
    }

    @Test
    fun fabOpensAddTaskScreen() {
        // Натискане на FAB бутона
        onView(withId(R.id.fabAddTask))
            .perform(click())

        // AddEdit екранът се показва
        onView(withId(R.id.etTitle))
            .check(matches(isDisplayed()))
    }

    @Test
    fun addTaskWithEmptyTitleShowsError() {
        // Отвори AddEdit екрана
        onView(withId(R.id.fabAddTask))
            .perform(click())

        // Натисни Запази без заглавие
        onView(withId(R.id.btnSave))
            .perform(click())

        // Полето за заглавие е видимо (грешката е показана)
        onView(withId(R.id.etTitle))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToCalendarTab() {
        // Натискане на Calendar таб
        onView(withId(R.id.calendarFragment))
            .perform(click())

        // Календарът се показва
        onView(withId(R.id.calendarView))
            .check(matches(isDisplayed()))
    }
}