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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [EmptyDeskKeeper]. Plain JUnit, runs on the host. */
class EmptyDeskKeeperTest {

    @Test
    fun shouldKeepOnLastWindowClose_desktopFirstHomeBehind_true() {
        assertTrue(createKeeper().shouldKeepOnLastWindowClose(DESKTOP_FIRST_DISPLAY))
    }

    @Test
    fun shouldKeepOnLastWindowClose_disabled_false() {
        assertFalse(
            createKeeper(isEnabled = false).shouldKeepOnLastWindowClose(DESKTOP_FIRST_DISPLAY)
        )
    }

    @Test
    fun shouldKeepOnLastWindowClose_touchFirstDisplay_false() {
        assertFalse(createKeeper().shouldKeepOnLastWindowClose(TOUCH_FIRST_DISPLAY))
    }

    @Test
    fun shouldKeepOnLastWindowClose_homeNotBehindDesktop_false() {
        assertFalse(
            createKeeper(isHomeBehindDesktop = false)
                .shouldKeepOnLastWindowClose(DESKTOP_FIRST_DISPLAY)
        )
    }

    @Test
    fun shouldKeepWhenHomeShows_emptyDeskOnlyHomeOpening_true() {
        assertTrue(
            createKeeper()
                .shouldKeepWhenHomeShows(
                    DESKTOP_FIRST_DISPLAY,
                    isDeskEmpty = true,
                    isOnlyHomeOpening = true,
                )
        )
    }

    @Test
    fun shouldKeepWhenHomeShows_deskWithWindows_false() {
        assertFalse(
            createKeeper()
                .shouldKeepWhenHomeShows(
                    DESKTOP_FIRST_DISPLAY,
                    isDeskEmpty = false,
                    isOnlyHomeOpening = true,
                )
        )
    }

    @Test
    fun shouldKeepWhenHomeShows_appOpening_false() {
        assertFalse(
            createKeeper()
                .shouldKeepWhenHomeShows(
                    DESKTOP_FIRST_DISPLAY,
                    isDeskEmpty = true,
                    isOnlyHomeOpening = false,
                )
        )
    }

    @Test
    fun shouldKeepWhenHomeShows_touchFirstDisplay_false() {
        assertFalse(
            createKeeper()
                .shouldKeepWhenHomeShows(
                    TOUCH_FIRST_DISPLAY,
                    isDeskEmpty = true,
                    isOnlyHomeOpening = true,
                )
        )
    }

    @Test
    fun disabled_neverKeeps() {
        assertFalse(
            EmptyDeskKeeper.DISABLED.shouldKeepWhenHomeShows(
                DESKTOP_FIRST_DISPLAY,
                isDeskEmpty = true,
                isOnlyHomeOpening = true,
            )
        )
        assertFalse(EmptyDeskKeeper.DISABLED.shouldKeepOnLastWindowClose(DESKTOP_FIRST_DISPLAY))
    }

    private fun createKeeper(isEnabled: Boolean = true, isHomeBehindDesktop: Boolean = true) =
        EmptyDeskKeeper(
            isEnabled = isEnabled,
            isHomeBehindDesktop = isHomeBehindDesktop,
            isDesktopFirst = IntPredicate { displayId -> displayId == DESKTOP_FIRST_DISPLAY },
        )

    private companion object {
        const val DESKTOP_FIRST_DISPLAY = 0
        const val TOUCH_FIRST_DISPLAY = 2
    }
}
