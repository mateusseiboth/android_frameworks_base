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

package com.android.systemui.statusbar.quickactions.calendar.domain.interactor

import android.content.applicationContext
import com.android.systemui.kosmos.Kosmos
import com.android.systemui.plugins.activityStarter
import com.android.systemui.statusbar.quickactions.calendar.data.repository.CalendarEventsRepository
import com.android.systemui.statusbar.quickactions.calendar.shared.model.CalendarEventModel
import com.android.systemui.statusbar.quickactions.calendar.ui.viewmodel.CalendarPopupViewModel
import com.android.systemui.statusbar.quickactions.domain.interactor.quickActionsInteractor

/** Calendar events a test sets, instead of the calendar provider's. */
class FakeCalendarEventsRepository : CalendarEventsRepository {
    var events: List<CalendarEventModel> = emptyList()

    override suspend fun findEvents(beginMillis: Long, endMillis: Long) =
        events.filter { it.beginMillis < endMillis && it.endMillis > beginMillis }
}

val Kosmos.fakeCalendarEventsRepository by Kosmos.Fixture { FakeCalendarEventsRepository() }

val Kosmos.calendarPopupViewModelFactory by
    Kosmos.Fixture {
        object : CalendarPopupViewModel.Factory {
            override fun create() =
                CalendarPopupViewModel(
                    eventsRepository = fakeCalendarEventsRepository,
                    activityStarter = activityStarter,
                    quickActionsInteractor = quickActionsInteractor,
                )
        }
    }

val Kosmos.calendarPopupInteractor by
    Kosmos.Fixture {
        CalendarPopupInteractor(
            context = applicationContext,
            quickActionsInteractor = quickActionsInteractor,
            popupViewModelFactory = calendarPopupViewModelFactory,
        )
    }
