/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.quickactions.calendar.domain.interactor

import android.graphics.RectF
import android.platform.test.annotations.EnableFlags
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.kosmos.runTest
import com.android.systemui.res.R
import com.android.systemui.scene.domain.interactor.sceneInteractor
import com.android.systemui.scene.shared.model.Overlays
import com.android.systemui.statusbar.quickactions.domain.interactor.quickActionsInteractor
import com.android.systemui.statusbar.quickactions.popups.StatusBarPopupChips
import com.android.systemui.statusbar.quickactions.shared.model.QuickActionChipId
import com.android.systemui.testKosmosNew
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@EnableFlags(StatusBarPopupChips.FLAG_NAME)
@RunWith(AndroidJUnit4::class)
class CalendarPopupInteractorTest : SysuiTestCase() {
    private val kosmos = testKosmosNew()

    @Test
    fun onClockClicked_enabled_opensTheCalendarUnderTheClock() =
        kosmos.runTest {
            setCalendarEnabled(true)
            val clockBounds = RectF(0f, 0f, 120f, 36f)

            calendarPopupInteractor.onClockBoundsChanged(clockBounds)
            calendarPopupInteractor.onClockClicked()

            assertThat(quickActionsInteractor.activePanel?.chipId)
                .isEqualTo(QuickActionChipId.Calendar)
            assertThat(quickActionsInteractor.activePanel?.anchorBounds).isEqualTo(clockBounds)
            assertThat(sceneInteractor.transitionState.currentOverlays)
                .contains(Overlays.QuickActions)
        }

    @Test
    fun onClockClicked_twice_closesTheCalendar() =
        kosmos.runTest {
            setCalendarEnabled(true)

            calendarPopupInteractor.onClockClicked()
            calendarPopupInteractor.onClockClicked()

            assertThat(quickActionsInteractor.activePanel).isNull()
        }

    @Test
    fun onClockClicked_disabled_opensNothing() =
        kosmos.runTest {
            setCalendarEnabled(false)

            calendarPopupInteractor.onClockClicked()

            assertThat(quickActionsInteractor.activePanel).isNull()
        }

    private fun setCalendarEnabled(enabled: Boolean) {
        context.orCreateTestableResources.addOverride(
            R.bool.config_statusBarClockOpensCalendar,
            enabled,
        )
    }
}
