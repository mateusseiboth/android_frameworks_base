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

package com.android.wm.shell.desktopmode.multidesks

import java.util.function.IntPredicate

/**
 * Decides when an empty desk stays active while Home shows, on a desktop-first display where Home
 * is the background of the desktop. Keeping the desk active makes the next app open as a window in
 * it instead of starting outside the desktop.
 */
class EmptyDeskKeeper(
    private val isEnabled: Boolean,
    private val isHomeBehindDesktop: Boolean,
    private val isDesktopFirst: IntPredicate,
) {

    /** Whether closing the last window of the active desk on [displayId] keeps the desk active. */
    fun shouldKeepOnLastWindowClose(displayId: Int): Boolean =
        isEnabled && isHomeBehindDesktop && isDesktopFirst.test(displayId)

    /**
     * Whether a desk on [displayId] that moved behind Home stays active: only when it holds no
     * window ([isDeskEmpty]) and nothing but Home came to front ([isOnlyHomeOpening]).
     */
    fun shouldKeepWhenHomeShows(
        displayId: Int,
        isDeskEmpty: Boolean,
        isOnlyHomeOpening: Boolean,
    ): Boolean = isDeskEmpty && isOnlyHomeOpening && shouldKeepOnLastWindowClose(displayId)

    companion object {
        /** A keeper that never keeps an empty desk active, the AOSP behavior. */
        @JvmField
        val DISABLED =
            EmptyDeskKeeper(
                isEnabled = false,
                isHomeBehindDesktop = false,
                isDesktopFirst = IntPredicate { false },
            )
    }
}
