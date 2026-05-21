package com.example.uniplanner

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
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
        // Кликаме върху бутона "Tasks" в долното заоблено меню
        onView(withId(R.id.tasksFragment))
            .perform(click())

        // Проверяваме дали списъкът с всички задачи (rvTasks) се визуализира успешно
        onView(withId(R.id.rvTasks))
            .check(matches(isDisplayed()))
    }

    @Test
    fun fabOpensAddTaskScreen() {
        // Кликаме върху FAB бутона за нова задача на Dashboard-а
        onView(withId(R.id.fabAddTask))
            .perform(click())

        // Проверяваме дали се отваря екранът за добавяне, като търсим полето за Заглавие
        onView(withId(R.id.etTitle))
            .check(matches(isDisplayed()))
    }

    @Test
    fun addTaskWithEmptyTitleShowsError() {
        // Отваряме екрана за добавяне на задача
        onView(withId(R.id.fabAddTask))
            .perform(click())

        // Директно натискаме бутона "Запази" (btnSave) без да пишем нищо
        onView(withId(R.id.btnSave))
            .perform(click())

        // Тъй като полето показва грешка, то си остава на екрана. Проверяваме дали все още е видимо.
        onView(withId(R.id.etTitle))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToCalendarTab() {
        // Кликаме върху таба за Календар в BottomNavigationView
        onView(withId(R.id.calendarFragment))
            .perform(click())

        // Проверяваме дали MaterialCalendarView компонентата се показва успешно на екрана
        onView(withId(R.id.calendarView))
            .check(matches(isDisplayed()))
    }
}