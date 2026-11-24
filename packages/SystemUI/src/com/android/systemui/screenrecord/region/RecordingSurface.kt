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

import android.graphics.Rect
import android.util.Size
import android.view.Surface

/** The surface a recording's virtual display draws into, with the size it must have. */
interface RecordingSurface {
    val surface: Surface
    val width: Int
    val height: Int

    /** Releases what this surface created; the encoder's own surface stays with its owner. */
    fun release()
}

/** Records the whole capture target: the virtual display draws straight into the encoder. */
class EncoderRecordingSurface(override val surface: Surface, size: Size) : RecordingSurface {
    override val width = size.width
    override val height = size.height

    override fun release() {}
}

/** Picks how a recording reaches the encoder. */
object RecordingSurfaces {

    /** The part of the display to record for [crop], or null to record the whole target. */
    @JvmStatic
    fun regionOf(crop: Rect?, displayWidth: Int, displayHeight: Int): PixelRegion? =
        crop?.let {
            RegionRecordingGeometry.fitToDisplay(
                PixelRegion(it.left, it.top, it.right, it.bottom),
                displayWidth,
                displayHeight,
            )
        }

    /** The size of the recorded picture, before the encoder's limits apply. */
    @JvmStatic
    fun recordedSizeOf(region: PixelRegion?, displayWidth: Int, displayHeight: Int): Size =
        region?.let { Size(it.width, it.height) } ?: Size(displayWidth, displayHeight)

    @JvmStatic
    fun create(
        encoderSurface: Surface,
        encoderSize: Size,
        displaySize: Size,
        region: PixelRegion?,
    ): RecordingSurface =
        region?.let { createCropped(encoderSurface, encoderSize, displaySize, it) }
            ?: EncoderRecordingSurface(encoderSurface, encoderSize)

    private fun createCropped(
        encoderSurface: Surface,
        encoderSize: Size,
        displaySize: Size,
        region: PixelRegion,
    ): RecordingSurface =
        CroppedRecordingSurface(
            encoderSurface = encoderSurface,
            encoderSize = encoderSize,
            displaySize = displaySize,
            window =
                RegionRecordingGeometry.textureWindowOf(
                    region,
                    displaySize.width,
                    displaySize.height,
                ),
        )
}
