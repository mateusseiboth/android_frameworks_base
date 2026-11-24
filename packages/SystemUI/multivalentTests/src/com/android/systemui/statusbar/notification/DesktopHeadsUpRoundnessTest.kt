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

package com.android.systemui.statusbar.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class DesktopHeadsUpRoundnessTest {

    @Test
    fun pinnedRow_isRound() {
        assertEquals(RoundnessRequest.ROUND, headsUpRoundness(state(isPinned = true)))
    }

    @Test
    fun rowAnimatingAway_staysRound() {
        assertEquals(RoundnessRequest.ROUND, headsUpRoundness(state(isAnimatingAway = true)))
    }

    @Test
    fun trackedRow_staysRound() {
        assertEquals(RoundnessRequest.ROUND, headsUpRoundness(state(isTracked = true)))
    }

    @Test
    fun rowBackInTheList_resetsItsRoundness() {
        assertEquals(RoundnessRequest.RESET, headsUpRoundness(state()))
    }

    @Test
    fun pulsingRoundness_isLeftAloneWhenPulsingRowsAreNotRounded() {
        val state = state(isShowingPulsing = true, shouldRoundPulsing = false)

        assertEquals(RoundnessRequest.UNCHANGED, pulsingRoundness(state))
    }

    @Test
    fun pulsingRow_isRoundWhenPulsingRowsAreRounded() {
        val state = state(isShowingPulsing = true, shouldRoundPulsing = true)

        assertEquals(RoundnessRequest.ROUND, pulsingRoundness(state))
    }

    @Test
    fun rowThatStoppedPulsing_resetsItsPulsingRoundness() {
        val state = state(isShowingPulsing = false, shouldRoundPulsing = true)

        assertEquals(RoundnessRequest.RESET, pulsingRoundness(state))
    }

    private fun state(
        isPinned: Boolean = false,
        isAnimatingAway: Boolean = false,
        isTracked: Boolean = false,
        isShowingPulsing: Boolean = false,
        shouldRoundPulsing: Boolean = false,
    ) =
        HeadsUpRowState(
            isPinned = isPinned,
            isAnimatingAway = isAnimatingAway,
            isTracked = isTracked,
            isShowingPulsing = isShowingPulsing,
            shouldRoundPulsing = shouldRoundPulsing,
        )
}
