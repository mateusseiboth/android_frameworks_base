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

package com.android.systemui.statusbar.quickactions.calendar.ui.compose

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.systemui.res.R
import com.android.systemui.statusbar.quickactions.calendar.shared.model.CalendarEventModel
import com.android.systemui.statusbar.quickactions.calendar.ui.viewmodel.CalendarPopupViewModel
import java.util.TimeZone

/** Calendar popup under the desktop status bar clock: the month, today marked, next events. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPopup(viewModel: CalendarPopupViewModel, modifier: Modifier = Modifier) {
    val now = remember { System.currentTimeMillis() }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = now)
    var events by remember { mutableStateOf(emptyList<CalendarEventModel>()) }
    LaunchedEffect(viewModel) { events = viewModel.findUpcomingEvents(now) }

    Surface(
        shape = RoundedCornerShape(CalendarPopup.CornerRadius),
        color = MaterialTheme.colorScheme.surfaceBright,
        shadowElevation = 2.dp,
        modifier = modifier.width(CalendarPopup.Width),
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                colors =
                    DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
            )
            UpcomingEvents(events = events, onEventClicked = viewModel::onEventClicked)
            TextButton(
                onClick = { viewModel.onDayOpened(datePickerState.selectedDateMillis ?: now) },
                modifier = Modifier.align(Alignment.End).padding(horizontal = 12.dp),
            ) {
                Text(stringResource(R.string.status_bar_calendar_open))
            }
        }
    }
}

@Composable
private fun UpcomingEvents(
    events: List<CalendarEventModel>,
    onEventClicked: (CalendarEventModel) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    ) {
        if (events.isEmpty()) {
            Text(
                text = stringResource(R.string.status_bar_calendar_no_events),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
            return@Column
        }
        events.forEach { event -> EventRow(event = event, onClick = { onEventClicked(event) }) }
    }
}

@Composable
private fun EventRow(event: CalendarEventModel, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Box(modifier = Modifier.size(8.dp).background(Color(event.color), CircleShape))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title ?: stringResource(R.string.status_bar_calendar_untitled_event),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatEventTime(context, event),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "Mon, Nov 24, 10:00 to 11:00", or "Mon, Nov 24, All day" for an all day event. */
private fun formatEventTime(context: Context, event: CalendarEventModel): String {
    val dateFlags =
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_ALL
    if (event.isAllDay) {
        // All day events are stored at midnight UTC.
        val day = DateUtils.formatDateRange(
            context,
            java.util.Formatter(),
            event.beginMillis,
            event.beginMillis,
            dateFlags,
            TimeZone.getTimeZone("UTC").id,
        )
        return context.getString(R.string.status_bar_calendar_all_day_event, day.toString())
    }
    return DateUtils.formatDateRange(
        context,
        event.beginMillis,
        event.endMillis,
        dateFlags or DateUtils.FORMAT_SHOW_TIME,
    )
}

private object CalendarPopup {
    val Width = 360.dp
    val CornerRadius = 28.dp
}
