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

import androidx.annotation.ColorInt

/** One occurrence of a calendar event, as the status bar calendar lists it. */
data class CalendarEventModel(
    val eventId: Long,
    val title: String?,
    val beginMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
    @ColorInt val color: Int,
)

/** Picks the events the status bar calendar shows under the month. */
object UpcomingCalendarEvents {

    private val byStart =
        compareBy<CalendarEventModel> { it.beginMillis }.thenBy { !it.isAllDay }.thenBy { it.eventId }

    /** Events not yet ended at [nowMillis], soonest first (all day ones first), at most [limit]. */
    fun select(
        events: List<CalendarEventModel>,
        nowMillis: Long,
        limit: Int,
    ): List<CalendarEventModel> =
        events.filter { it.endMillis > nowMillis }.sortedWith(byStart).take(limit)
}
