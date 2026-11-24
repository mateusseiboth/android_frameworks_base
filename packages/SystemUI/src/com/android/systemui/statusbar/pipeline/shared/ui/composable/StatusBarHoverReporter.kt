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

package com.android.systemui.statusbar.pipeline.shared.ui.composable

import kotlin.math.hypot

/**
 * Decides which pointer hover events over the desktop status bar are worth reporting: the first
 * hover, a move of at least [thresholdPx] since the last report, and the exit after a hover.
 */
class StatusBarHoverReporter(private val thresholdPx: Float) {
    private var lastX = Float.NaN
    private var lastY = Float.NaN

    private val isHovering: Boolean
        get() = !lastX.isNaN()

    /** Whether the hover at ([x], [y]) is reported; if so, it becomes the last reported one. */
    fun shouldReportMove(x: Float, y: Float): Boolean {
        if (isHovering && hypot(x - lastX, y - lastY) < thresholdPx) return false
        lastX = x
        lastY = y
        return true
    }

    /** Whether the pointer leaving the status bar is reported. */
    fun shouldReportExit(): Boolean {
        if (!isHovering) return false
        lastX = Float.NaN
        lastY = Float.NaN
        return true
    }
}
