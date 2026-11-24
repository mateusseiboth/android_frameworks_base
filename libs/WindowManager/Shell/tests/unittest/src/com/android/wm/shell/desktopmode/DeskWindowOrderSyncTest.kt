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

package com.android.wm.shell.desktopmode

import android.app.ActivityManager.RunningTaskInfo
import android.testing.AndroidTestingRunner
import androidx.test.filters.SmallTest
import com.android.wm.shell.ShellTestCase
import com.android.wm.shell.TestRunningTaskInfoBuilder
import com.android.wm.shell.desktopmode.data.DesktopRepository
import com.android.wm.shell.desktopmode.multidesks.DesksOrganizer
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for [DeskWindowOrderSync].
 *
 * Build/Install/Run: atest WMShellUnitTests:DeskWindowOrderSyncTest
 */
@SmallTest
@RunWith(AndroidTestingRunner::class)
class DeskWindowOrderSyncTest : ShellTestCase() {
    private val desksOrganizer = mock<DesksOrganizer>()
    private val repository = mock<DesktopRepository>()

    @Test
    fun applyWindowOrder_enabled_appliesOrderOfDeskTasks() {
        val inDeskTop = createTask(taskId = 3)
        val otherDesk = createTask(taskId = 9)
        val inDeskBottom = createTask(taskId = 1)
        whenever(desksOrganizer.getDeskIdFromTaskInfo(inDeskTop)).thenReturn(DESK_ID)
        whenever(desksOrganizer.getDeskIdFromTaskInfo(otherDesk)).thenReturn(OTHER_DESK_ID)
        whenever(desksOrganizer.getDeskIdFromTaskInfo(inDeskBottom)).thenReturn(DESK_ID)
        val sync =
            DeskWindowOrderSync(isEnabled = true, desksOrganizer) {
                listOf(inDeskTop, otherDesk, inDeskBottom)
            }

        sync.applyWindowOrder(repository, DESK_ID, DISPLAY_ID)

        verify(repository).applyWindowOrderToDesk(DESK_ID, listOf(3, 1))
    }

    @Test
    fun applyWindowOrder_disabled_doesNothing() {
        val sync = DeskWindowOrderSync(isEnabled = false, desksOrganizer) { listOf(createTask(3)) }

        sync.applyWindowOrder(repository, DESK_ID, DISPLAY_ID)

        verify(repository, never()).applyWindowOrderToDesk(any(), any())
    }

    private fun createTask(taskId: Int): RunningTaskInfo =
        TestRunningTaskInfoBuilder().setTaskId(taskId).build()

    private companion object {
        const val DESK_ID = 4
        const val OTHER_DESK_ID = 5
        const val DISPLAY_ID = 0
    }
}
