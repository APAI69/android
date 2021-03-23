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

package com.owncloud.android.settings.security

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.owncloud.android.R
import com.owncloud.android.authentication.BiometricManager
import com.owncloud.android.presentation.UIResult
import com.owncloud.android.presentation.ui.settings.fragments.SettingsFragment
import com.owncloud.android.presentation.viewmodels.settings.SettingsViewModel
import com.owncloud.android.ui.activity.BiometricActivity
import com.owncloud.android.ui.activity.PassCodeActivity
import com.owncloud.android.ui.activity.PatternLockActivity
import com.owncloud.android.utils.mockIntent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class SettingsFragmentSecuritySectionTest {

    private lateinit var fragmentScenario: FragmentScenario<SettingsFragment>

    private lateinit var prefLogsCategory: PreferenceCategory
    private lateinit var prefMoreCategory: PreferenceCategory
    private lateinit var prefSecurityCategory: PreferenceCategory
    private lateinit var prefPasscode: CheckBoxPreference
    private lateinit var prefPattern: CheckBoxPreference
    private lateinit var prefBiometric: CheckBoxPreference
    private lateinit var prefTouchesWithOtherVisibleWindows: CheckBoxPreference

    private lateinit var biometricManager: BiometricManager

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var context: Context

    private val passCodeValue = "1111"
    private val patternValue = "1234"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        settingsViewModel = mockk(relaxed = true)
        mockkStatic(BiometricManager::class)
        biometricManager = mockk()

        every { BiometricManager.getBiometricManager(any()) } returns biometricManager
        every { biometricManager.onActivityStarted(any()) } returns Unit
        every { biometricManager.onActivityStopped(any()) } returns Unit

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

        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_ownCloud)
        fragmentScenario.onFragment { fragment ->
            prefLogsCategory = fragment.findPreference(PREFERENCE_LOGS_CATEGORY)!!
            prefMoreCategory = fragment.findPreference(PREFERENCE_MORE_CATEGORY)!!
            prefSecurityCategory = fragment.findPreference(PREFERENCE_SECURITY_CATEGORY)!!
            prefPasscode = fragment.findPreference(PassCodeActivity.PREFERENCE_SET_PASSCODE)!!
            prefPattern = fragment.findPreference(PatternLockActivity.PREFERENCE_SET_PATTERN)!!
            prefBiometric = fragment.findPreference(BiometricActivity.PREFERENCE_SET_BIOMETRIC)!!
            prefTouchesWithOtherVisibleWindows =
                fragment.findPreference(SettingsFragment.PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS)!!
        }

        prefLogsCategory.isVisible = false
        prefMoreCategory.isVisible = false

        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
    }

    @Test
    fun securityView() {
        onView(withText(R.string.prefs_category_security)).check(matches(isDisplayed()))
        assertEquals(PREFERENCE_SECURITY_CATEGORY, prefSecurityCategory.key)
        assertEquals(context.getString(R.string.prefs_category_security), prefSecurityCategory.title)
        assertEquals(null, prefSecurityCategory.summary)
        assertTrue(prefSecurityCategory.isVisible)

        onView(withText(R.string.prefs_passcode)).check(matches(isDisplayed()))
        assertEquals(PassCodeActivity.PREFERENCE_SET_PASSCODE, prefPasscode.key)
        assertEquals(context.getString(R.string.prefs_passcode), prefPasscode.title)
        assertEquals(null, prefPasscode.summary)
        assertTrue(prefPasscode.isVisible)
        assertTrue(prefPasscode.isEnabled)
        assertFalse(prefPasscode.isChecked)

        onView(withText(R.string.prefs_pattern)).check(matches(isDisplayed()))
        assertEquals(PatternLockActivity.PREFERENCE_SET_PATTERN, prefPattern.key)
        assertEquals(context.getString(R.string.prefs_pattern), prefPattern.title)
        assertEquals(null, prefPattern.summary)
        assertTrue(prefPattern.isVisible)
        assertTrue(prefPattern.isEnabled)
        assertFalse(prefPattern.isChecked)

        onView(withText(R.string.prefs_biometric)).check(matches(isDisplayed()))
        onView(withText(R.string.prefs_biometric_summary)).check(matches(isDisplayed()))
        onView(withText(R.string.prefs_biometric)).check(matches(not(isEnabled())))
        assertEquals(BiometricActivity.PREFERENCE_SET_BIOMETRIC, prefBiometric.key)
        assertEquals(context.getString(R.string.prefs_biometric), prefBiometric.title)
        assertEquals(context.getString(R.string.prefs_biometric_summary), prefBiometric.summary)
        assertTrue(prefBiometric.isVisible)
        assertFalse(prefBiometric.isEnabled)
        assertFalse(prefBiometric.isChecked)

        onView(withText(R.string.prefs_touches_with_other_visible_windows)).check(matches(isDisplayed()))
        onView(withText(R.string.prefs_touches_with_other_visible_windows_summary)).check(matches(isDisplayed()))
        assertEquals(SettingsFragment.PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS, prefTouchesWithOtherVisibleWindows.key)
        assertEquals(
            context.getString(R.string.prefs_touches_with_other_visible_windows),
            prefTouchesWithOtherVisibleWindows.title
        )
        assertEquals(
            context.getString(R.string.prefs_touches_with_other_visible_windows_summary),
            prefTouchesWithOtherVisibleWindows.summary
        )
        assertTrue(prefTouchesWithOtherVisibleWindows.isVisible)
        assertTrue(prefTouchesWithOtherVisibleWindows.isEnabled)
        assertFalse(prefTouchesWithOtherVisibleWindows.isChecked)
    }

    @Test
    fun passcodeOpen() {
        onView(withText(R.string.prefs_passcode)).perform(click())
        intended(hasComponent(PassCodeActivity::class.java.name))
    }

    @Test
    fun patternOpen() {
        onView(withText(R.string.prefs_pattern)).perform(click())
        intended(hasComponent(PatternLockActivity::class.java.name))
    }

    @Test
    fun passcodeLockEnabledOk() {
        every { settingsViewModel.isPatternSet() } returns false
        every { settingsViewModel.handleEnablePasscode(any()) } returns UIResult.Success()

        mockIntent(
            extras = Pair(PassCodeActivity.KEY_PASSCODE, passCodeValue),
            action = PassCodeActivity.ACTION_REQUEST_WITH_RESULT
        )
        onView(withText(R.string.prefs_passcode)).perform(click())
        assertTrue(prefPasscode.isChecked)
    }

    @Test
    fun passcodeLockEnabledError() {
        every { settingsViewModel.isPatternSet() } returns false
        every { settingsViewModel.handleEnablePasscode(any()) } returns UIResult.Error()

        mockIntent(
            extras = Pair(PassCodeActivity.KEY_PASSCODE, passCodeValue),
            action = PassCodeActivity.ACTION_REQUEST_WITH_RESULT
        )
        onView(withText(R.string.prefs_passcode)).perform(click())
        assertFalse(prefPasscode.isChecked)
        onView(withText(R.string.pass_code_error_set)).check(matches(isDisplayed()))
    }

    @Test
    fun patternLockEnabledOk() {
        every { settingsViewModel.isPasscodeSet() } returns false
        every { settingsViewModel.handleEnablePattern(any())} returns UIResult.Success()

        mockIntent(
            extras = Pair(PatternLockActivity.KEY_PATTERN, patternValue),
            action = PatternLockActivity.ACTION_REQUEST_WITH_RESULT
        )
        onView(withText(R.string.prefs_pattern)).perform(click())
        assertTrue(prefPattern.isChecked)
    }

    @Test
    fun patternLockEnabledError() {
        every { settingsViewModel.isPasscodeSet() } returns false
        every { settingsViewModel.handleEnablePattern(any())} returns UIResult.Error()

        mockIntent(
            extras = Pair(PatternLockActivity.KEY_PATTERN, patternValue),
            action = PatternLockActivity.ACTION_REQUEST_WITH_RESULT
        )
        onView(withText(R.string.prefs_pattern)).perform(click())
        assertFalse(prefPattern.isChecked)
        onView(withText(R.string.pattern_error_set)).check(matches(isDisplayed()))
    }

    @Test
    fun enablePasscodeEnablesBiometricLock() {
        firstEnablePasscode()
        onView(withText(R.string.prefs_biometric)).check(matches(isEnabled()))
        assertTrue(prefBiometric.isEnabled)
        assertFalse(prefBiometric.isChecked)
    }

    @Test
    fun enablePatternEnablesBiometricLock() {
        firstEnablePattern()
        onView(withText(R.string.prefs_biometric)).check(matches(isEnabled()))
        assertTrue(prefBiometric.isEnabled)
        assertFalse(prefBiometric.isChecked)
    }

    @Test
    fun onlyOneMethodEnabledPattern() {
        every { settingsViewModel.isPatternSet() } returns true

        firstEnablePattern()
        onView(withText(R.string.prefs_passcode)).perform(click())
        onView(withText(R.string.pattern_already_set)).check(matches(isEnabled()))
    }

    @Test
    fun onlyOneMethodEnabledPasscode() {
        every { settingsViewModel.isPasscodeSet() } returns true

        firstEnablePasscode()
        onView(withText(R.string.prefs_pattern)).perform(click())
        onView(withText(R.string.passcode_already_set)).check(matches(isEnabled()))
    }

    @Test
    fun disablePasscodeOk() {
        every { settingsViewModel.handleDisablePasscode(any())} returns UIResult.Success()

        firstEnablePasscode()
        mockIntent(
            extras = Pair(PassCodeActivity.KEY_CHECK_RESULT, true),
            action = PassCodeActivity.ACTION_CHECK_WITH_RESULT
        )
        onView(withText(R.string.prefs_passcode)).perform(click())
        assertFalse(prefPasscode.isChecked)
        onView(withText(R.string.prefs_biometric)).check(matches(not(isEnabled())))
        assertFalse(prefBiometric.isEnabled)
        assertFalse(prefBiometric.isChecked)
    }

    @Test
    fun disablePasscodeError() {
        every { settingsViewModel.handleDisablePasscode(any())} returns UIResult.Error()

        firstEnablePasscode()
        mockIntent(
            extras = Pair(PassCodeActivity.KEY_CHECK_RESULT, true),
            action = PassCodeActivity.ACTION_CHECK_WITH_RESULT
        )
        onView(withText(R.string.prefs_passcode)).perform(click())
        assertTrue(prefPasscode.isChecked)
        onView(withText(R.string.prefs_biometric)).check(matches(isEnabled()))
        assertTrue(prefBiometric.isEnabled)
        onView(withText(R.string.pass_code_error_remove)).check(matches(isDisplayed()))
    }

    @Test
    fun disablePatternOk() {
        every { settingsViewModel.handleDisablePattern(any())} returns UIResult.Success()

        firstEnablePattern()
        mockIntent(
            extras = Pair(PatternLockActivity.KEY_CHECK_RESULT, true),
            action = PatternLockActivity.ACTION_CHECK_WITH_RESULT
        )
        onView(withText(R.string.prefs_pattern)).perform(click())
        assertFalse(prefPattern.isChecked)
        onView(withText(R.string.prefs_biometric)).check(matches(not(isEnabled())))
        assertFalse(prefBiometric.isEnabled)
        assertFalse(prefBiometric.isChecked)
    }

    @Test
    fun disablePatternError() {
        every { settingsViewModel.handleDisablePattern(any())} returns UIResult.Error()

        firstEnablePattern()
        mockIntent(
            extras = Pair(PatternLockActivity.KEY_CHECK_RESULT, true),
            action = PatternLockActivity.ACTION_CHECK_WITH_RESULT
        )
        onView(withText(R.string.prefs_pattern)).perform(click())
        assertTrue(prefPattern.isChecked)
        onView(withText(R.string.prefs_biometric)).check(matches(isEnabled()))
        assertTrue(prefBiometric.isEnabled)
        onView(withText(R.string.pattern_error_remove)).check(matches(isDisplayed()))
    }

    @Test
    fun enableBiometricLockWithPasscodeEnabled() {
        every { biometricManager.isHardwareDetected } returns true
        every { biometricManager.hasEnrolledBiometric() } returns true

        firstEnablePasscode()
        onView(withText(R.string.prefs_biometric)).perform(click())
        assertTrue(prefBiometric.isChecked)
    }

    @Test
    fun enableBiometricLockWithPatternEnabled() {
        every { biometricManager.isHardwareDetected } returns true
        every { biometricManager.hasEnrolledBiometric() } returns true

        firstEnablePattern()
        onView(withText(R.string.prefs_biometric)).perform(click())
        assertTrue(prefBiometric.isChecked)
    }

    @Test
    fun enableBiometricLockHardwareNotDetected() {
        every { biometricManager.isHardwareDetected } returns false

        firstEnablePasscode()
        onView(withText(R.string.prefs_biometric)).perform(click())
        assertFalse(prefBiometric.isChecked)
        onView(withText(R.string.biometric_not_hardware_detected)).check(matches(isEnabled()))
    }

    @Test
    fun enableBiometricLockNoEnrolledBiometric() {
        every { biometricManager.isHardwareDetected } returns true
        every { biometricManager.hasEnrolledBiometric() } returns false

        firstEnablePasscode()
        onView(withText(R.string.prefs_biometric)).perform(click())
        assertFalse(prefBiometric.isChecked)
        onView(withText(R.string.biometric_not_enrolled)).check(matches(isEnabled()))
    }

    @Test
    fun disableBiometricLock() {
        every { biometricManager.isHardwareDetected } returns true
        every { biometricManager.hasEnrolledBiometric() } returns true

        firstEnablePasscode()
        onView(withText(R.string.prefs_biometric)).perform(click())
        onView(withText(R.string.prefs_biometric)).perform(click())
        assertFalse(prefBiometric.isChecked)
    }

    @Test
    fun touchesDialog() {
        onView(withText(R.string.prefs_touches_with_other_visible_windows)).perform(click())
        onView(withText(R.string.confirmation_touches_with_other_windows_title)).check(matches(isDisplayed()))
        onView(withText(R.string.confirmation_touches_with_other_windows_message)).check(matches(isDisplayed()))
    }

    @Test
    fun touchesEnable() {
        every { settingsViewModel.setPrefTouchesWithOtherVisibleWindows(any())} returns Unit

        onView(withText(R.string.prefs_touches_with_other_visible_windows)).perform(click())
        onView(withText(R.string.common_yes)).perform(click())
        assertTrue(prefTouchesWithOtherVisibleWindows.isChecked)
    }

    @Test
    fun touchesRefuse() {
        onView(withText(R.string.prefs_touches_with_other_visible_windows)).perform(click())
        onView(withText(R.string.common_no)).perform(click())
        assertFalse(prefTouchesWithOtherVisibleWindows.isChecked)
    }

    @Test
    fun touchesDisable() {
        every { settingsViewModel.setPrefTouchesWithOtherVisibleWindows(any())} returns Unit

        onView(withText(R.string.prefs_touches_with_other_visible_windows)).perform(click())
        onView(withText(R.string.common_yes)).perform(click())
        onView(withText(R.string.prefs_touches_with_other_visible_windows)).perform(click())
        assertFalse(prefTouchesWithOtherVisibleWindows.isChecked)
    }

    private fun firstEnablePasscode() {
        every { settingsViewModel.isPatternSet() } returns false
        every { settingsViewModel.handleEnablePasscode(any())} returns UIResult.Success()

        mockIntent(
            extras = Pair(PassCodeActivity.KEY_PASSCODE, passCodeValue),
            action = PassCodeActivity.ACTION_REQUEST_WITH_RESULT
        )
        onView(withText(R.string.prefs_passcode)).perform(click())
    }

    private fun firstEnablePattern() {
        every { settingsViewModel.isPasscodeSet() } returns false
        every { settingsViewModel.handleEnablePattern(any())} returns UIResult.Success()

        mockIntent(
            extras = Pair(PatternLockActivity.KEY_PATTERN, patternValue),
            action = PatternLockActivity.ACTION_REQUEST_WITH_RESULT
        )
        onView(withText(R.string.prefs_pattern)).perform(click())
    }

    companion object {
        private const val PREFERENCE_SECURITY_CATEGORY = "security_category"
        private const val PREFERENCE_LOGS_CATEGORY = "logs_category"
        private const val PREFERENCE_MORE_CATEGORY = "more_category"
    }
}