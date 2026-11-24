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
package com.android.wm.shell.desktopmode

import com.android.wm.shell.desktopmode.FocusedWindowDeskResolver.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for [FocusedWindowDeskResolver].
 *
 * Usage: atest WMShellUnitTests:FocusedWindowDeskResolverTest
 */
class FocusedWindowDeskResolverTest {
    // Desks 10, 11 and 12 in this order on one display.
    private val desks = listOf(10, 11, 12)
    private val resolver =
        FocusedWindowDeskResolver(
            previousDeskOf = { deskId -> desks.getOrNull(desks.indexOf(deskId) - 1) },
            nextDeskOf = { deskId -> desks.getOrNull(desks.indexOf(deskId) + 1) },
        )

    @Test
    fun previous_isTheDeskBefore() {
        assertEquals(10, resolver.findTargetDeskId(currentDeskId = 11, Direction.PREVIOUS))
    }

    @Test
    fun next_isTheDeskAfter() {
        assertEquals(12, resolver.findTargetDeskId(currentDeskId = 11, Direction.NEXT))
    }

    @Test
    fun previousOfTheFirstDesk_isNone() {
        assertNull(resolver.findTargetDeskId(currentDeskId = 10, Direction.PREVIOUS))
    }

    @Test
    fun nextOfTheLastDesk_isNone() {
        assertNull(resolver.findTargetDeskId(currentDeskId = 12, Direction.NEXT))
    }

    @Test
    fun windowOutsideAnyDesk_hasNoTarget() {
        assertNull(resolver.findTargetDeskId(currentDeskId = null, Direction.NEXT))
    }
}
