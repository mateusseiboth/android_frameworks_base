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

package com.android.systemui.statusbar.quickactions.calendar.shared.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class UpcomingCalendarEventsTest : SysuiTestCase() {

    @Test
    fun select_dropsEventsThatEnded() {
        val ended = event(id = 1, begin = 0, end = 50)
        val ongoing = event(id = 2, begin = 50, end = 150)

        assertThat(UpcomingCalendarEvents.select(listOf(ended, ongoing), nowMillis = 100, limit = 5))
            .containsExactly(ongoing)
    }

    @Test
    fun select_sortsByBeginAndKeepsTheFirstOnes() {
        val later = event(id = 1, begin = 300, end = 400)
        val sooner = event(id = 2, begin = 200, end = 250)
        val latest = event(id = 3, begin = 500, end = 600)

        assertThat(
                UpcomingCalendarEvents.select(
                    listOf(later, sooner, latest),
                    nowMillis = 100,
                    limit = 2,
                )
            )
            .containsExactly(sooner, later)
            .inOrder()
    }

    @Test
    fun select_putsAllDayEventsFirstOnTheSameStart() {
        val timed = event(id = 1, begin = 200, end = 300)
        val allDay = event(id = 2, begin = 200, end = 1000, isAllDay = true)

        assertThat(UpcomingCalendarEvents.select(listOf(timed, allDay), nowMillis = 100, limit = 5))
            .containsExactly(allDay, timed)
            .inOrder()
    }

    private fun event(id: Long, begin: Long, end: Long, isAllDay: Boolean = false) =
        CalendarEventModel(
            eventId = id,
            title = "Event $id",
            beginMillis = begin,
            endMillis = end,
            isAllDay = isAllDay,
            color = 0,
        )
}
