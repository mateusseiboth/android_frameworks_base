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

package com.android.systemui.statusbar.notification

import android.testing.TestableLooper.RunWithLooper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.kosmos.useUnconfinedTestDispatcher
import com.android.systemui.shade.ShadeHeadsUpTracker
import com.android.systemui.shade.shadeViewController
import com.android.systemui.statusbar.notification.collection.NotificationEntry
import com.android.systemui.statusbar.notification.collection.buildNotificationEntry
import com.android.systemui.statusbar.notification.headsup.PinnedStatus
import com.android.systemui.statusbar.notification.headsup.mockHeadsUpManager
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
import com.android.systemui.statusbar.notification.row.createRowWithEntry
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayoutController
import com.android.systemui.testKosmos
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@SmallTest
@RunWith(AndroidJUnit4::class)
@RunWithLooper
class DesktopHeadsUpAppearanceControllerTest : SysuiTestCase() {
    private val kosmos = testKosmos().useUnconfinedTestDispatcher()

    private val stackScrollerController = mock<NotificationStackScrollLayoutController>()
    private val shadeViewController = kosmos.shadeViewController
    private val shadeHeadsUpTracker = mock<ShadeHeadsUpTracker>()
    private val notificationRoundnessManager = mock<NotificationRoundnessManager>()
    private val headsUpManager = kosmos.mockHeadsUpManager

    private lateinit var entry: NotificationEntry
    private lateinit var row: ExpandableNotificationRow
    private lateinit var underTest: DesktopHeadsUpAppearanceController

    @Before
    fun setUp() {
        allowTestableLooperAsMainThread()
        entry = kosmos.buildNotificationEntry()
        row = kosmos.createRowWithEntry(entry)
        whenever(shadeViewController.shadeHeadsUpTracker).thenReturn(shadeHeadsUpTracker)
        underTest =
            DesktopHeadsUpAppearanceController(
                headsUpManager,
                stackScrollerController,
                shadeViewController,
                notificationRoundnessManager,
            )
    }

    @Test
    fun register_addsTheListeners() {
        underTest.register()

        verify(headsUpManager).addListener(underTest)
        verify(shadeHeadsUpTracker).addTrackingHeadsUpListener(any())
        verify(stackScrollerController).addOnExpandedHeightChangedListener(any())
        verify(stackScrollerController).addHeadsUpAnimatingAwayListener(any())
    }

    @Test
    fun unregister_removesTheListeners() {
        underTest.register()

        underTest.unregister()

        verify(headsUpManager).removeListener(underTest)
        verify(shadeHeadsUpTracker).removeTrackingHeadsUpListener(any())
        verify(stackScrollerController).removeOnExpandedHeightChangedListener(any())
        verify(stackScrollerController).removeHeadsUpAnimatingAwayListener(any())
    }

    @Test
    fun pinnedHeadsUp_isFullyRoundedAndShowsItsHeader() {
        row.setPinnedStatus(PinnedStatus.PinnedBySystem)
        row.setHeaderVisibleAmount(0f)

        underTest.onHeadsUpPinned(entry)

        assertThat(row.topRoundness).isWithin(0.001f).of(1f)
        assertThat(row.bottomRoundness).isWithin(0.001f).of(1f)
        assertThat(row.headerVisibleAmount).isWithin(0.001f).of(1f)
    }

    @Test
    fun unpinnedHeadsUp_returnsToTheListRoundness() {
        row.setPinnedStatus(PinnedStatus.PinnedBySystem)
        underTest.onHeadsUpPinned(entry)

        row.setPinnedStatus(PinnedStatus.NotPinned)
        underTest.onHeadsUpUnPinned(entry)

        assertThat(row.topRoundness).isWithin(0.001f).of(0f)
        assertThat(row.bottomRoundness).isWithin(0.001f).of(0f)
    }
}
