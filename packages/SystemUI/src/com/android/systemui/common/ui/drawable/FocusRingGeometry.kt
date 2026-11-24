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

package com.android.systemui.common.ui.drawable

import kotlin.math.max
import kotlin.math.min

/**
 * The visible shape of a focusable view: its [cornerRadius] and how far it sits inside the view's
 * bounds (a button background is often inset to keep a larger touch target). In pixels.
 */
data class FocusRingShape(
    val cornerRadius: Float,
    val insetLeft: Float = 0f,
    val insetTop: Float = 0f,
    val insetRight: Float = 0f,
    val insetBottom: Float = 0f,
) {
    companion object {
        /** A pill or a circle: the corners are half the shortest side. */
        const val FULLY_ROUNDED = Float.POSITIVE_INFINITY
    }
}

/** The center line of a focus ring stroke inside a view, in pixels. */
data class FocusRingBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val radius: Float,
)

/**
 * Places a Material 3 focus ring: a stroke that follows the shape at a gap, with concentric corners,
 * and never leaves the view's bounds so that parents clipping their children do not cut it.
 */
object FocusRingGeometry {

    fun find(
        width: Float,
        height: Float,
        shape: FocusRingShape,
        strokeWidth: Float,
        gap: Float,
    ): FocusRingBounds {
        val halfStroke = strokeWidth / 2
        val outset = gap + halfStroke
        val left = max(shape.insetLeft - outset, halfStroke)
        val top = max(shape.insetTop - outset, halfStroke)
        val right = min(width - shape.insetRight + outset, width - halfStroke)
        val bottom = min(height - shape.insetBottom + outset, height - halfStroke)
        val maxRadius = max(0f, min(right - left, bottom - top) / 2)
        val radius = (shape.cornerRadius + outset).coerceIn(0f, maxRadius)
        return FocusRingBounds(left, top, right, bottom, radius)
    }
}
