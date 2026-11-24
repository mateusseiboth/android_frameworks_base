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

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for [DeskZOrder]. Plain JUnit, runs on the host. */
class DeskZOrderTest {

    @Test
    fun mergeWindowOrder_noLiveTasks_keepsRecordedOrder() {
        assertEquals(
            listOf(1, 2, 3),
            DeskZOrder.mergeWindowOrder(recordedTopFirst = listOf(1, 2, 3), liveTopFirst = listOf()),
        )
    }

    @Test
    fun mergeWindowOrder_allLive_takesLiveOrder() {
        assertEquals(
            listOf(3, 1, 2),
            DeskZOrder.mergeWindowOrder(
                recordedTopFirst = listOf(1, 2, 3),
                liveTopFirst = listOf(3, 1, 2),
            ),
        )
    }

    @Test
    fun mergeWindowOrder_someNotRunning_keepTheirSlots() {
        assertEquals(
            listOf(4, 9, 1),
            DeskZOrder.mergeWindowOrder(
                recordedTopFirst = listOf(1, 9, 4),
                liveTopFirst = listOf(4, 1),
            ),
        )
    }

    @Test
    fun mergeWindowOrder_liveTaskNotRecorded_ignored() {
        assertEquals(
            listOf(2, 1),
            DeskZOrder.mergeWindowOrder(
                recordedTopFirst = listOf(1, 2),
                liveTopFirst = listOf(7, 2, 1),
            ),
        )
    }

    @Test
    fun mergeWindowOrder_duplicateLiveTask_countedOnce() {
        assertEquals(
            listOf(2, 1),
            DeskZOrder.mergeWindowOrder(
                recordedTopFirst = listOf(1, 2),
                liveTopFirst = listOf(2, 2, 1),
            ),
        )
    }
}
