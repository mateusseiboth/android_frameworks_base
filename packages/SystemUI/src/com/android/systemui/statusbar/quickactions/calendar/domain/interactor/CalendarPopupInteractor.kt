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

import android.content.Context
import android.graphics.RectF
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.res.R
import com.android.systemui.statusbar.quickactions.calendar.ui.viewmodel.CalendarPopupViewModel
import com.android.systemui.statusbar.quickactions.domain.interactor.QuickActionsInteractor
import com.android.systemui.statusbar.quickactions.shared.model.QuickActionChipId
import com.android.systemui.statusbar.quickactions.shared.model.QuickActionPanelModel
import javax.inject.Inject

/** Opens and closes the calendar popup of the desktop status bar's clock. */
@SysUISingleton
class CalendarPopupInteractor
@Inject
constructor(
    @Application private val context: Context,
    private val quickActionsInteractor: QuickActionsInteractor,
    private val popupViewModelFactory: CalendarPopupViewModel.Factory,
) {
    private var clockBounds = RectF()

    private val isEnabled: Boolean
        get() = context.resources.getBoolean(R.bool.config_statusBarClockOpensCalendar)

    /** Remembers where the clock is, to anchor the popup under it. */
    fun onClockBoundsChanged(bounds: RectF) {
        clockBounds = RectF(bounds)
    }

    /** Toggles the calendar popup under the clock. */
    fun onClockClicked() {
        if (!isEnabled) return
        quickActionsInteractor.toggle(
            QuickActionPanelModel(
                chipId = QuickActionChipId.Calendar,
                anchorBounds = clockBounds,
                panelContentViewModelFactory = popupViewModelFactory,
            )
        )
    }
}
