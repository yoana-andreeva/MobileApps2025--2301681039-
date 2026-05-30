package com.example.uniplanner

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
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
        // Проверяваме дали RecyclerView за предстоящи задачи се вижда при старт
        onView(withId(R.id.rvUpcomingTasks))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNavigationWorking() {
        // Клик върху таба за задачи в Bottom Navigation
        onView(withId(R.id.tasksFragment)).perform(click())

        // Проверяваме дали се вижда Empty State съобщението, тъй като базата е празна
        onView(withId(R.id.emptyStateTasks))
            .check(matches(isDisplayed()))
    }

    @Test
    fun fabOpensAddTaskScreen() {
        // Отиваме на екрана със задачите
        onView(withId(R.id.tasksFragment)).perform(click())

        // Клик върху FAB бутона за добавяне на задача
        onView(withId(R.id.fabAddTask)).perform(click())

        // Проверяваме дали успешно сме навигирали до екрана за създаване
        onView(withId(R.id.tvScreenTitle))
            .check(matches(withText("Нова задача")))
    }

    @Test
    fun addTaskWithEmptyTitleShowsError() {
        // 1. Отиваме на екрана със задачите
        onView(withId(R.id.tasksFragment)).perform(click())

        // 2. Клик върху FAB бутона за добавяне на задача
        onView(withId(R.id.fabAddTask)).perform(click())

        // 3. Оставяме заглавието празно и директно натискаме бутона "Създай задача"
        onView(withId(R.id.btnSave)).perform(click())

        // 4. Проверяваме дали TextInputLayout или TextInputEditText показва съответната грешка
        onView(withId(R.id.etTitle)).check(matches(hasErrorText("Заглавието е задължително")))
    }

    @Test
    fun navigateToCalendarTab() {
        // 1. Клик върху картата на седмичния календар в DashboardFragment
        onView(withId(R.id.cardCalendarStrip)).perform(click())

        // 2. Проверяваме дали успешно сме отишли в CalendarFragment,
        // като потвърждаваме, че неговият RecyclerView (rvDayTasks) е зареден на екрана
        onView(withId(R.id.rvDayTasks)).check(matches(isDisplayed()))
    }
}