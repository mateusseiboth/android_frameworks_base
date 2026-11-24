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

package com.android.wm.shell.desktopmode.data

import com.android.wm.shell.desktopmode.data.MinimizedTiledTasks.TiledSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [MinimizedTiledTasks]. Plain JUnit, runs on the host. */
class MinimizedTiledTasksTest {
    private val tasks = MinimizedTiledTasks()

    @Test
    fun onMinimized_leftTiledTask_restoresLeft() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = TASK_ID, rightTiledTaskId = OTHER_TASK_ID)

        assertEquals(
            TiledSide.LEFT,
            tasks.takeSideToRestore(TASK_ID, leftTiledTaskId = null, rightTiledTaskId = 7),
        )
    }

    @Test
    fun onMinimized_rightTiledTask_restoresRight() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = OTHER_TASK_ID, rightTiledTaskId = TASK_ID)

        assertEquals(
            TiledSide.RIGHT,
            tasks.takeSideToRestore(
                TASK_ID,
                leftTiledTaskId = OTHER_TASK_ID,
                rightTiledTaskId = null,
            ),
        )
    }

    @Test
    fun onMinimized_untiledTask_nothingToRestore() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = OTHER_TASK_ID, rightTiledTaskId = null)

        assertFalse(tasks.contains(TASK_ID))
        assertNull(
            tasks.takeSideToRestore(TASK_ID, leftTiledTaskId = null, rightTiledTaskId = null)
        )
    }

    @Test
    fun takeSideToRestore_sideTakenByAnotherTask_nothingToRestore() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = TASK_ID, rightTiledTaskId = null)

        assertNull(
            tasks.takeSideToRestore(
                TASK_ID,
                leftTiledTaskId = OTHER_TASK_ID,
                rightTiledTaskId = null,
            )
        )
    }

    @Test
    fun takeSideToRestore_onlyOnce() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = TASK_ID, rightTiledTaskId = null)
        tasks.takeSideToRestore(TASK_ID, leftTiledTaskId = null, rightTiledTaskId = null)

        assertNull(
            tasks.takeSideToRestore(TASK_ID, leftTiledTaskId = null, rightTiledTaskId = null)
        )
    }

    @Test
    fun takeSideToRestore_sideTakenByAnotherTask_forgetsTask() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = TASK_ID, rightTiledTaskId = null)
        tasks.takeSideToRestore(TASK_ID, leftTiledTaskId = OTHER_TASK_ID, rightTiledTaskId = null)

        assertFalse(tasks.contains(TASK_ID))
    }

    @Test
    fun remove_forgetsTask() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = TASK_ID, rightTiledTaskId = null)

        tasks.remove(TASK_ID)

        assertFalse(tasks.contains(TASK_ID))
    }

    @Test
    fun contains_rememberedTask_true() {
        tasks.onMinimized(TASK_ID, leftTiledTaskId = null, rightTiledTaskId = TASK_ID)

        assertTrue(tasks.contains(TASK_ID))
    }

    private companion object {
        const val TASK_ID = 21
        const val OTHER_TASK_ID = 22
    }
}
