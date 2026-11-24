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

package com.android.systemui.statusbar.quickactions.calendar.ui.viewmodel

import android.content.ContentUris
import android.content.Intent
import android.provider.CalendarContract
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.statusbar.quickactions.calendar.data.repository.CalendarEventsRepository
import com.android.systemui.statusbar.quickactions.calendar.shared.model.CalendarEventModel
import com.android.systemui.statusbar.quickactions.calendar.shared.model.UpcomingCalendarEvents
import com.android.systemui.statusbar.quickactions.domain.interactor.QuickActionsInteractor
import com.android.systemui.statusbar.quickactions.popups.ui.viewmodel.StatusBarPopupViewModel
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/** Calendar popup of the desktop status bar clock: the month and the next events. */
class CalendarPopupViewModel
@AssistedInject
constructor(
    private val eventsRepository: CalendarEventsRepository,
    private val activityStarter: ActivityStarter,
    private val quickActionsInteractor: QuickActionsInteractor,
) : StatusBarPopupViewModel {

    /** The next events from now, for the list under the month. */
    suspend fun findUpcomingEvents(nowMillis: Long): List<CalendarEventModel> =
        UpcomingCalendarEvents.select(
            eventsRepository.findEvents(nowMillis, nowMillis + UPCOMING_WINDOW_MILLIS),
            nowMillis,
            MAX_EVENTS,
        )

    /** Opens the event in the calendar app. */
    fun onEventClicked(event: CalendarEventModel) {
        startAndClose(
            Intent(Intent.ACTION_VIEW)
                .setData(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId)
                )
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.beginMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endMillis)
        )
    }

    /** Opens the calendar app on the given day. */
    fun onDayOpened(dayMillis: Long) {
        val uri =
            CalendarContract.CONTENT_URI.buildUpon()
                .appendPath("time")
                .also { ContentUris.appendId(it, dayMillis) }
                .build()
        startAndClose(Intent(Intent.ACTION_VIEW).setData(uri))
    }

    private fun startAndClose(intent: Intent) {
        quickActionsInteractor.close()
        activityStarter.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), true)
    }

    @AssistedFactory
    interface Factory : StatusBarPopupViewModel.Factory.Calendar {
        override fun create(): CalendarPopupViewModel
    }

    private companion object {
        val UPCOMING_WINDOW_MILLIS = TimeUnit.DAYS.toMillis(7)
        const val MAX_EVENTS = 5
    }
}
