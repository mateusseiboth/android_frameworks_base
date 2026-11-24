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
package com.android.wm.shell.recents

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [DesktopTaskFocusHistory].
 *
 * Usage: atest WMShellUnitTests:DesktopTaskFocusHistoryTest
 */
class DesktopTaskFocusHistoryTest {
    private val history = DesktopTaskFocusHistory()

    @Test
    fun neverFocusedTask_keepsItsActiveTime() {
        assertEquals(40L, history.getLastFocusedTime(TASK_A, /* fallbackTime= */ 40L))
    }

    @Test
    fun focusedTask_reportsItsLastFocusTime() {
        history.onTaskFocused(TASK_A, 10L)
        history.onTaskFocused(TASK_A, 30L)

        assertEquals(30L, history.getLastFocusedTime(TASK_A, /* fallbackTime= */ 50L))
    }

    @Test
    fun removedTask_isForgotten() {
        history.onTaskFocused(TASK_A, 10L)

        history.onTaskRemoved(TASK_A)

        assertEquals(50L, history.getLastFocusedTime(TASK_A, /* fallbackTime= */ 50L))
    }

    @Test
    fun orderDeskTasks_sortsDeskTasksByTimeMostRecentFirst() {
        val tasks = listOf(Item(TASK_A, 10L), Item(TASK_B, 30L), Item(TASK_C, 20L))

        val ordered = history.orderDeskTasks(tasks, { true }, { it.time })

        assertEquals(listOf(TASK_B, TASK_C, TASK_A), ordered.map { it.id })
    }

    @Test
    fun orderDeskTasks_keepsOtherTasksInTheirSlots() {
        val tasks =
            listOf(
                Item(TASK_A, 10L, isDesk = true),
                Item(FULLSCREEN, 5L, isDesk = false),
                Item(TASK_B, 30L, isDesk = true),
            )

        val ordered = history.orderDeskTasks(tasks, { it.isDesk }, { it.time })

        assertEquals(listOf(TASK_B, FULLSCREEN, TASK_A), ordered.map { it.id })
    }

    @Test
    fun orderDeskTasks_keepsTheGivenOrderOnEqualTimes() {
        val tasks = listOf(Item(TASK_A, 10L), Item(TASK_B, 10L))

        val ordered = history.orderDeskTasks(tasks, { true }, { it.time })

        assertEquals(listOf(TASK_A, TASK_B), ordered.map { it.id })
    }

    private data class Item(val id: Int, val time: Long, val isDesk: Boolean = true)

    private companion object {
        const val TASK_A = 1
        const val TASK_B = 2
        const val TASK_C = 3
        const val FULLSCREEN = 4
    }
}
