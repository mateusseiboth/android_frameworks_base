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
package com.android.wm.shell.windowdecor.viewholder

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Offsets and timing of the fullscreen header sliding down from the top edge when shown and back
 * up when hidden. A slide reversed midway continues from where the header is, taking the share of
 * [DURATION_MS] that the remaining distance represents.
 */
class FullscreenHeaderSlide(private val durationMs: Long = DURATION_MS) {

    /** A slide of the header's translationY from [fromY] to [toY] lasting [durationMs]. */
    data class Plan(val fromY: Float, val toY: Float, val durationMs: Long)

    /** The translationY that puts a header of [height] pixels fully above the top edge. */
    fun hiddenOffset(height: Int): Float = -height.toFloat()

    /** Plans the slide toward shown ([isShowing]) or hidden from the current [currentY]. */
    fun plan(isShowing: Boolean, currentY: Float, height: Int): Plan {
        val toY = targetOffset(isShowing, height)
        if (height <= 0) return Plan(toY, toY, durationMs = 0)
        val fromY = currentY.coerceIn(hiddenOffset(height), 0f)
        val duration = (durationMs * abs(toY - fromY) / height).roundToLong()
        return Plan(fromY, toY, duration)
    }

    private fun targetOffset(isShowing: Boolean, height: Int): Float =
        when {
            isShowing || height <= 0 -> 0f
            else -> hiddenOffset(height)
        }

    companion object {
        /** Duration of a full slide, as on Googlebook. */
        const val DURATION_MS = 250L
    }
}
