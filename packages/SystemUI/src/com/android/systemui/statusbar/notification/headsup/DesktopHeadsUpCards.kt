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

package com.android.systemui.statusbar.notification.headsup

import android.content.res.Resources
import androidx.compose.ui.Alignment
import com.android.systemui.res.R

/**
 * Heads-up notifications as cards at the top end of a PC's screen (config_desktopHeadsUpCards), as
 * on desktop systems: a card shows for a fixed time ([timeoutMs]) and leaves, whatever it is (no
 * sticky card, no call or full screen card that stays); the pointer over it, expanding it, replying
 * or its settings restart that time once, never more; several cards stack, at most [maxCards], the
 * oldest leaving first. Each one stays in the notifications list afterwards.
 */
class DesktopHeadsUpCards(val isEnabled: Boolean, val timeoutMs: Int, val maxCards: Int) {

    /** How long a heads-up shows before it leaves. */
    fun findAutoDismissTime(defaultMs: Int): Int = timeoutMs.takeIf { isEnabled } ?: defaultMs

    /** Cards sit at the top end while the notifications popup is closed; the popup keeps its side. */
    fun findStackAlignment(
        shadeAlignment: Alignment.Horizontal,
        isShadeExpanded: Boolean,
    ): Alignment.Horizontal = Alignment.End.takeIf { isEnabled && !isShadeExpanded } ?: shadeAlignment

    companion object {
        @JvmStatic
        fun fromResources(resources: Resources): DesktopHeadsUpCards =
            DesktopHeadsUpCards(
                isEnabled = resources.getBoolean(R.bool.config_desktopHeadsUpCards),
                timeoutMs = resources.getInteger(R.integer.config_desktopHeadsUpTimeoutMs),
                maxCards = resources.getInteger(R.integer.config_desktopHeadsUpMaxCards),
            )
    }
}
