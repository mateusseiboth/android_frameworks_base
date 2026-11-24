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

/** Tests for [RememberedBoundsTilingFilter]. Plain JUnit, runs on the host. */
class RememberedBoundsTilingFilterTest {

    @Test
    fun shouldRemember_untiledTask_true() {
        val filter = RememberedBoundsTilingFilter(isTiledExcluded = true)

        assertTrue(filter.shouldRemember(TASK_ID, leftTiledTaskId = 7, rightTiledTaskId = 8))
    }

    @Test
    fun shouldRemember_noTiling_true() {
        val filter = RememberedBoundsTilingFilter(isTiledExcluded = true)

        assertTrue(filter.shouldRemember(TASK_ID, leftTiledTaskId = null, rightTiledTaskId = null))
    }

    @Test
    fun shouldRemember_leftTiledTask_false() {
        val filter = RememberedBoundsTilingFilter(isTiledExcluded = true)

        assertFalse(filter.shouldRemember(TASK_ID, leftTiledTaskId = TASK_ID, rightTiledTaskId = 8))
    }

    @Test
    fun shouldRemember_rightTiledTask_false() {
        val filter = RememberedBoundsTilingFilter(isTiledExcluded = true)

        assertFalse(
            filter.shouldRemember(TASK_ID, leftTiledTaskId = null, rightTiledTaskId = TASK_ID)
        )
    }

    @Test
    fun shouldRemember_tiledTask_exclusionOff_true() {
        val filter = RememberedBoundsTilingFilter(isTiledExcluded = false)

        assertTrue(filter.shouldRemember(TASK_ID, leftTiledTaskId = TASK_ID, rightTiledTaskId = 8))
    }

    private companion object {
        const val TASK_ID = 5
    }
}
