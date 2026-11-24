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

package com.android.systemui.statusbar.quickactions.calendar.data.repository

import android.Manifest.permission.READ_CALENDAR
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.Cursor
import android.provider.CalendarContract.Instances
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.statusbar.quickactions.calendar.shared.model.CalendarEventModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Reads the user's calendar events. */
interface CalendarEventsRepository {
    /** Occurrences of visible events between [beginMillis] and [endMillis]. */
    suspend fun findEvents(beginMillis: Long, endMillis: Long): List<CalendarEventModel>
}

/** Reads [Instances] through the calendar provider; nothing without READ_CALENDAR. */
@SysUISingleton
class CalendarEventsRepositoryImpl
@Inject
constructor(
    @Application private val context: Context,
    @Background private val bgDispatcher: CoroutineDispatcher,
) : CalendarEventsRepository {

    override suspend fun findEvents(beginMillis: Long, endMillis: Long): List<CalendarEventModel> {
        if (context.checkSelfPermission(READ_CALENDAR) != PERMISSION_GRANTED) return emptyList()
        return withContext(bgDispatcher) { queryInstances(beginMillis, endMillis) }
    }

    private fun queryInstances(beginMillis: Long, endMillis: Long): List<CalendarEventModel> {
        val uri =
            Instances.CONTENT_URI.buildUpon()
                .also { ContentUris.appendId(it, beginMillis) }
                .also { ContentUris.appendId(it, endMillis) }
                .build()
        val cursor =
            context.contentResolver.query(
                uri,
                PROJECTION,
                "${Instances.VISIBLE} = 1",
                null,
                "${Instances.BEGIN} ASC",
            ) ?: return emptyList()
        return cursor.use { generateSequence { it.takeIf(Cursor::moveToNext) }.map(::readEvent).toList() }
    }

    private fun readEvent(cursor: Cursor) =
        CalendarEventModel(
            eventId = cursor.getLong(INDEX_EVENT_ID),
            title = cursor.getString(INDEX_TITLE),
            beginMillis = cursor.getLong(INDEX_BEGIN),
            endMillis = cursor.getLong(INDEX_END),
            isAllDay = cursor.getInt(INDEX_ALL_DAY) != 0,
            color = cursor.getInt(INDEX_COLOR),
        )

    private companion object {
        val PROJECTION =
            arrayOf(
                Instances.EVENT_ID,
                Instances.TITLE,
                Instances.BEGIN,
                Instances.END,
                Instances.ALL_DAY,
                Instances.DISPLAY_COLOR,
            )
        const val INDEX_EVENT_ID = 0
        const val INDEX_TITLE = 1
        const val INDEX_BEGIN = 2
        const val INDEX_END = 3
        const val INDEX_ALL_DAY = 4
        const val INDEX_COLOR = 5
    }
}
