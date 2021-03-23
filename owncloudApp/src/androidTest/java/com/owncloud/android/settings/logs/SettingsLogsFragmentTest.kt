/**
 * ownCloud Android client application
 *
 * @author Juan Carlos Garrote Gascón
 *
 * Copyright (C) 2021 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.settings.logs

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.owncloud.android.R
import com.owncloud.android.presentation.ui.settings.LogHistoryActivity
import com.owncloud.android.presentation.ui.settings.fragments.SettingsLogsFragment
import com.owncloud.android.presentation.viewmodels.settings.SettingsViewModel
import com.owncloud.android.utils.matchers.verifyPreference
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class SettingsLogsFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<SettingsLogsFragment>

    private lateinit var prefEnableLogging: SwitchPreferenceCompat
    private lateinit var prefHttpLogs: CheckBoxPreference
    private lateinit var prefLogsView: Preference

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        settingsViewModel = mockk(relaxUnitFun = true)

        stopKoin()

        startKoin {
            context
            modules(
                module(override = true) {
                    viewModel {
                        settingsViewModel
                    }
                }
            )
        }

        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        unmockkAll()
    }

    private fun launchTest(enabledLogging: Boolean) {
        every { settingsViewModel.isLoggingEnabled() } returns enabledLogging

        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_ownCloud)
        fragmentScenario.onFragment { fragment ->
            prefEnableLogging = fragment.findPreference(SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING)!!
            prefHttpLogs = fragment.findPreference(SettingsLogsFragment.PREFERENCE_LOG_HTTP)!!
            prefLogsView = fragment.findPreference(SettingsLogsFragment.PREFERENCE_LOGGER)!!
        }
    }

    @Test
    fun logsViewLoggingDisabled() {
        launchTest(enabledLogging = false)

        prefEnableLogging.verifyPreference(
            keyPref = SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING,
            titlePref = context.getString(R.string.prefs_enable_logging),
            summaryPref = context.getString(R.string.prefs_enable_logging_summary),
            visible = true,
            enabled = true
        )
        prefHttpLogs.verifyPreference(
            keyPref = SettingsLogsFragment.PREFERENCE_LOG_HTTP,
            titlePref = context.getString(R.string.prefs_http_logs),
            visible = false
        )
        prefLogsView.verifyPreference(
            keyPref = SettingsLogsFragment.PREFERENCE_LOGGER,
            titlePref = context.getString(R.string.log_open_logs_view),
            visible = false
        )
    }

    @Test
    fun logsViewLoggingEnabled() {
        launchTest(enabledLogging = true)

        prefEnableLogging.verifyPreference(
            keyPref = SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING,
            titlePref = context.getString(R.string.prefs_enable_logging),
            summaryPref = context.getString(R.string.prefs_enable_logging_summary),
            visible = true,
            enabled = true
        )
        prefHttpLogs.verifyPreference(
            keyPref = SettingsLogsFragment.PREFERENCE_LOG_HTTP,
            titlePref = context.getString(R.string.prefs_http_logs),
            visible = true
        )
        prefLogsView.verifyPreference(
            keyPref = SettingsLogsFragment.PREFERENCE_LOGGER,
            titlePref = context.getString(R.string.log_open_logs_view),
            visible = true
        )
    }

    @Test
    fun enableLoggingMakesSettingsAppear() {
        launchTest(enabledLogging = false)

        onView(withText(R.string.prefs_enable_logging)).perform(click())
        onView(withText(R.string.prefs_http_logs)).check(matches(isDisplayed()))
        assertTrue(prefHttpLogs.isVisible)
        onView(withText(R.string.log_open_logs_view)).check(matches(isDisplayed()))
        assertTrue(prefLogsView.isVisible)
    }

    @Test
    fun disableLoggingMakesSettingsDisappear() {
        launchTest(enabledLogging = false)

        onView(withText(R.string.prefs_enable_logging)).perform(click())
        onView(withText(R.string.prefs_enable_logging)).perform(click())
        assertFalse(prefHttpLogs.isVisible)
        assertFalse(prefLogsView.isVisible)
    }

    @Test
    fun checkHttpLogs() {
        launchTest(enabledLogging = true)

        onView(withText(R.string.prefs_http_logs)).perform(click())
        assertTrue(prefHttpLogs.isChecked)
    }

    @Test
    fun disableLoggingMakesHttpLogsNotChecked() {
        launchTest(enabledLogging = false)

        onView(withText(R.string.prefs_enable_logging)).perform(click())
        onView(withText(R.string.prefs_http_logs)).perform(click())
        onView(withText(R.string.prefs_enable_logging)).perform(click())
        assertFalse(prefHttpLogs.isChecked)
    }

    @Test
    fun loggerOpen() {
        launchTest(enabledLogging = true)

        onView(withText(R.string.log_open_logs_view)).perform(click())
        intended(hasComponent(LogHistoryActivity::class.java.name))
    }

}