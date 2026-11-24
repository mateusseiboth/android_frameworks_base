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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [BackClosedTasks]. Plain JUnit, runs on the host. */
class BackClosedTasksTest {
    private val tasks = BackClosedTasks()

    @Test
    fun takeRemovalSkip_unknownTask_returnsFalse() {
        assertFalse(tasks.takeRemovalSkip(TASK_ID))
    }

    @Test
    fun takeRemovalSkip_afterAdd_returnsTrueOnce() {
        tasks.add(TASK_ID)

        assertTrue(tasks.takeRemovalSkip(TASK_ID))
        assertFalse(tasks.takeRemovalSkip(TASK_ID))
    }

    @Test
    fun takeRemovalSkip_otherTask_keepsSkipOfAddedTask() {
        tasks.add(TASK_ID)

        assertFalse(tasks.takeRemovalSkip(OTHER_TASK_ID))
        assertTrue(tasks.takeRemovalSkip(TASK_ID))
    }

    @Test
    fun remove_dropsPendingSkip() {
        tasks.add(TASK_ID)

        tasks.remove(TASK_ID)

        assertFalse(tasks.takeRemovalSkip(TASK_ID))
    }

    @Test
    fun contains_reflectsAddAndRemove() {
        tasks.add(TASK_ID)
        assertTrue(tasks.contains(TASK_ID))

        tasks.remove(TASK_ID)
        assertFalse(tasks.contains(TASK_ID))
    }

    @Test
    fun toString_listsTaskIds() {
        tasks.add(TASK_ID)

        assertEquals("[$TASK_ID]", tasks.toString())
    }

    private companion object {
        const val TASK_ID = 12
        const val OTHER_TASK_ID = 13
    }
}
