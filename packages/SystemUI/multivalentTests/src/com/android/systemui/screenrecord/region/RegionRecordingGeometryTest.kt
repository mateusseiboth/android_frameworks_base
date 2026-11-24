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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class RegionRecordingGeometryTest {

    @Test
    fun fitToDisplay_regionInside_isUnchanged() {
        val region = PixelRegion(10, 20, 110, 220)

        assertEquals(region, RegionRecordingGeometry.fitToDisplay(region, 1000, 800))
    }

    @Test
    fun fitToDisplay_regionPastTheEdges_isClipped() {
        val region = PixelRegion(-50, 700, 1200, 900)

        assertEquals(
            PixelRegion(0, 700, 1000, 800),
            RegionRecordingGeometry.fitToDisplay(region, 1000, 800),
        )
    }

    @Test
    fun fitToDisplay_regionOutsideTheDisplay_isNull() {
        assertNull(RegionRecordingGeometry.fitToDisplay(PixelRegion(1100, 0, 1200, 50), 1000, 800))
    }

    @Test
    fun fitToDisplay_emptyRegion_isNull() {
        assertNull(RegionRecordingGeometry.fitToDisplay(PixelRegion(10, 10, 10, 50), 1000, 800))
    }

    @Test
    fun textureWindowOf_wholeDisplay_coversTheTexture() {
        assertEquals(
            TextureWindow(offsetX = 0f, offsetY = 0f, scaleX = 1f, scaleY = 1f),
            RegionRecordingGeometry.textureWindowOf(PixelRegion(0, 0, 1000, 800), 1000, 800),
        )
    }

    @Test
    fun textureWindowOf_topLeftQuarter_startsHalfwayUpTheTexture() {
        // Texture coordinates grow upwards, display coordinates grow downwards.
        assertEquals(
            TextureWindow(offsetX = 0f, offsetY = 0.5f, scaleX = 0.5f, scaleY = 0.5f),
            RegionRecordingGeometry.textureWindowOf(PixelRegion(0, 0, 500, 400), 1000, 800),
        )
    }

    @Test
    fun textureWindowOf_bottomRightStrip() {
        assertEquals(
            TextureWindow(offsetX = 0.75f, offsetY = 0f, scaleX = 0.25f, scaleY = 0.25f),
            RegionRecordingGeometry.textureWindowOf(PixelRegion(750, 600, 1000, 800), 1000, 800),
        )
    }

    @Test
    fun cropMatrix_mapsTheUnitSquareOntoTheWindow() {
        val window = TextureWindow(offsetX = 0.25f, offsetY = 0.5f, scaleX = 0.5f, scaleY = 0.25f)
        val matrix = window.toCropMatrix()

        assertPoint(0.25f, 0.5f, matrix, 0f, 0f)
        assertPoint(0.75f, 0.75f, matrix, 1f, 1f)
        assertPoint(0.5f, 0.625f, matrix, 0.5f, 0.5f)
    }

    @Test
    fun cropMatrix_isColumnMajorWithSixteenValues() {
        assertEquals(16, TextureWindow(0f, 0f, 1f, 1f).toCropMatrix().size)
    }

    private fun assertPoint(
        expectedU: Float,
        expectedV: Float,
        matrix: FloatArray,
        u: Float,
        v: Float,
    ) {
        // Column-major 4x4 times the column vector (u, v, 0, 1).
        val mappedU = matrix[0] * u + matrix[4] * v + matrix[12]
        val mappedV = matrix[1] * u + matrix[5] * v + matrix[13]
        assertEquals(expectedU, mappedU, 1e-6f)
        assertEquals(expectedV, mappedV, 1e-6f)
    }
}
