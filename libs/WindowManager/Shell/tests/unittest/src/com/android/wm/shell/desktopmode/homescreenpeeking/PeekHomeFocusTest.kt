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

package com.android.wm.shell.desktopmode.homescreenpeeking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [PeekHomeFocus]. Plain JUnit, runs on the host. */
class PeekHomeFocusTest {
    private val focus = PeekHomeFocus()

    @Test
    fun takeTasksToRestore_returnsPeekedTasksOnce() {
        focus.onPeek(listOf(3, 2, 1))

        assertEquals(listOf(3, 2, 1), focus.takeTasksToRestore())
        assertEquals(emptyList<Int>(), focus.takeTasksToRestore())
    }

    @Test
    fun onPeek_secondPeek_keepsTasksOfFirst() {
        focus.onPeek(listOf(3))
        focus.onPeek(listOf(4, 3))

        assertEquals(listOf(3, 4), focus.takeTasksToRestore())
    }

    @Test
    fun shouldUnpeekOnTaskToFront_peekedTask_true() {
        focus.onPeek(listOf(3, 2))

        assertTrue(focus.shouldUnpeekOnTaskToFront(2))
    }

    @Test
    fun shouldUnpeekOnTaskToFront_otherTask_false() {
        focus.onPeek(listOf(3, 2))

        assertFalse(focus.shouldUnpeekOnTaskToFront(9))
    }

    @Test
    fun shouldUnpeekOnTaskToFront_afterRestore_false() {
        focus.onPeek(listOf(3, 2))
        focus.takeTasksToRestore()

        assertFalse(focus.shouldUnpeekOnTaskToFront(2))
    }

    @Test
    fun buildReorderOrder_noFrontTask_bottomFirstSoTopEndsOnTop() {
        assertEquals(
            listOf(1, 2, 3),
            PeekHomeFocus.buildReorderOrder(tasksTopFirst = listOf(3, 2, 1), frontTaskId = null),
        )
    }

    @Test
    fun buildReorderOrder_frontTask_movedLast() {
        assertEquals(
            listOf(1, 3, 2),
            PeekHomeFocus.buildReorderOrder(tasksTopFirst = listOf(3, 2, 1), frontTaskId = 2),
        )
    }

    @Test
    fun buildReorderOrder_frontTaskNotListed_ignored() {
        assertEquals(
            listOf(1, 3),
            PeekHomeFocus.buildReorderOrder(tasksTopFirst = listOf(3, 1), frontTaskId = 7),
        )
    }
}
