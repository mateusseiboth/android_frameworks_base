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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [DesktopTaskFocusSnapshotPolicy].
 *
 * Usage: atest WMShellUnitTests:DesktopTaskFocusSnapshotPolicyTest
 */
class DesktopTaskFocusSnapshotPolicyTest {
    private val policy = DesktopTaskFocusSnapshotPolicy(minIntervalMillis = 1_000L)

    @Test
    fun visibleDesktopTaskLosingFocus_isSnapshotted() {
        assertTrue(loseFocus(TASK, nowMillis = 0L))
    }

    @Test
    fun taskGainingFocus_isNotSnapshotted() {
        assertFalse(
            policy.shouldSnapshot(
                TASK,
                isFocusedGlobally = true,
                isVisibleDesktopTask = true,
                nowMillis = 0L,
            )
        )
    }

    @Test
    fun taskOutsideTheDesktop_isNotSnapshotted() {
        assertFalse(
            policy.shouldSnapshot(
                TASK,
                isFocusedGlobally = false,
                isVisibleDesktopTask = false,
                nowMillis = 0L,
            )
        )
    }

    @Test
    fun secondFocusLossWithinTheInterval_isNotSnapshotted() {
        loseFocus(TASK, nowMillis = 0L)

        assertFalse(loseFocus(TASK, nowMillis = 999L))
    }

    @Test
    fun focusLossAfterTheInterval_isSnapshottedAgain() {
        loseFocus(TASK, nowMillis = 0L)

        assertTrue(loseFocus(TASK, nowMillis = 1_000L))
    }

    @Test
    fun otherTaskWithinTheInterval_isSnapshotted() {
        loseFocus(TASK, nowMillis = 0L)

        assertTrue(loseFocus(OTHER_TASK, nowMillis = 10L))
    }

    @Test
    fun rejectedFocusLoss_doesNotStartTheInterval() {
        policy.shouldSnapshot(
            TASK,
            isFocusedGlobally = true,
            isVisibleDesktopTask = true,
            nowMillis = 0L,
        )

        assertTrue(loseFocus(TASK, nowMillis = 10L))
    }

    private fun loseFocus(taskId: Int, nowMillis: Long): Boolean =
        policy.shouldSnapshot(
            taskId,
            isFocusedGlobally = false,
            isVisibleDesktopTask = true,
            nowMillis = nowMillis,
        )

    private companion object {
        const val TASK = 1
        const val OTHER_TASK = 2
    }
}
