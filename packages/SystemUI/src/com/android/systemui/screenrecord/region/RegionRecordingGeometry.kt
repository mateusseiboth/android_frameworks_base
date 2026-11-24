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

package com.android.systemui.screenrecord.region

/** A rectangle in display pixels; [right] and [bottom] are exclusive. */
data class PixelRegion(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int
        get() = right - left

    val height: Int
        get() = bottom - top

    val isEmpty: Boolean
        get() = width <= 0 || height <= 0
}

/**
 * The part of a texture holding the whole display that a region recording samples, in texture
 * coordinates (0 to 1, origin at the bottom left).
 */
data class TextureWindow(
    val offsetX: Float,
    val offsetY: Float,
    val scaleX: Float,
    val scaleY: Float,
) {
    /** Column-major 4x4 matrix mapping the unit square onto this window. */
    fun toCropMatrix(): FloatArray =
        floatArrayOf(
            scaleX,
            0f,
            0f,
            0f,
            0f,
            scaleY,
            0f,
            0f,
            0f,
            0f,
            1f,
            0f,
            offsetX,
            offsetY,
            0f,
            1f,
        )
}

/** Geometry of recording a region of a display instead of the whole display. */
object RegionRecordingGeometry {

    /** The part of [region] inside a display of the given size, or null when nothing is left. */
    fun fitToDisplay(region: PixelRegion, displayWidth: Int, displayHeight: Int): PixelRegion? {
        val fitted =
            PixelRegion(
                left = region.left.coerceAtLeast(0),
                top = region.top.coerceAtLeast(0),
                right = region.right.coerceAtMost(displayWidth),
                bottom = region.bottom.coerceAtMost(displayHeight),
            )
        return fitted.takeUnless { it.isEmpty }
    }

    /** Where [region] lies in the texture of a display of the given size. */
    fun textureWindowOf(region: PixelRegion, displayWidth: Int, displayHeight: Int): TextureWindow {
        val width = displayWidth.toFloat()
        val height = displayHeight.toFloat()
        return TextureWindow(
            offsetX = region.left / width,
            offsetY = (displayHeight - region.bottom) / height,
            scaleX = region.width / width,
            scaleY = region.height / height,
        )
    }
}
