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

/** What a heads-up row asks of one of its roundness sources. */
enum class RoundnessRequest {
    ROUND,
    RESET,
    UNCHANGED,
}

/** The state of a heads-up row that decides its roundness. */
data class HeadsUpRowState(
    val isPinned: Boolean,
    val isAnimatingAway: Boolean,
    /** Whether the user is pulling the shade down from this row. */
    val isTracked: Boolean,
    val isShowingPulsing: Boolean,
    /** Whether pulsing rows are rounded on this device. */
    val shouldRoundPulsing: Boolean,
)

/**
 * A heads-up row is fully rounded while it floats over the screen: pinned, animating away, or
 * pulled down by the user. Back in the list it returns to the roundness of its neighbors.
 */
fun headsUpRoundness(state: HeadsUpRowState): RoundnessRequest {
    if (state.isPinned || state.isAnimatingAway || state.isTracked) return RoundnessRequest.ROUND
    return RoundnessRequest.RESET
}

/** A pulsing row is fully rounded, where this device rounds pulsing rows at all. */
fun pulsingRoundness(state: HeadsUpRowState): RoundnessRequest {
    if (!state.shouldRoundPulsing) return RoundnessRequest.UNCHANGED
    if (state.isShowingPulsing) return RoundnessRequest.ROUND
    return RoundnessRequest.RESET
}
