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
import android.graphics.SurfaceTexture
import android.util.Size
import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class RecordingSurfacesTest : SysuiTestCase() {

    private val encoderSize = Size(640, 360)
    private val displaySize = Size(1920, 1080)
    private lateinit var encoderTexture: SurfaceTexture
    private lateinit var encoderSurface: Surface

    @Before
    fun setUp() {
        encoderTexture = SurfaceTexture(/* singleBufferMode= */ false)
        encoderTexture.setDefaultBufferSize(encoderSize.width, encoderSize.height)
        encoderSurface = Surface(encoderTexture)
    }

    @After
    fun tearDown() {
        encoderSurface.release()
        encoderTexture.release()
    }

    @Test
    fun regionOf_noCrop_isNull() {
        assertThat(RecordingSurfaces.regionOf(null, 1920, 1080)).isNull()
    }

    @Test
    fun regionOf_crop_isFittedToTheDisplay() {
        assertThat(RecordingSurfaces.regionOf(Rect(-10, 100, 700, 1200), 1920, 1080))
            .isEqualTo(PixelRegion(0, 100, 700, 1080))
    }

    @Test
    fun recordedSizeOf_noRegion_isTheDisplay() {
        assertThat(RecordingSurfaces.recordedSizeOf(null, 1920, 1080)).isEqualTo(Size(1920, 1080))
    }

    @Test
    fun recordedSizeOf_region_isTheRegion() {
        assertThat(RecordingSurfaces.recordedSizeOf(PixelRegion(100, 50, 740, 410), 1920, 1080))
            .isEqualTo(Size(640, 360))
    }

    @Test
    fun create_withoutRegion_drawsStraightIntoTheEncoder() {
        val surface = RecordingSurfaces.create(encoderSurface, encoderSize, displaySize, null)

        assertThat(surface.surface).isSameInstanceAs(encoderSurface)
        assertThat(surface.width).isEqualTo(encoderSize.width)
        assertThat(surface.height).isEqualTo(encoderSize.height)
        surface.release()
    }

    @Test
    fun create_withRegion_takesTheWholeDisplay() {
        val region = PixelRegion(100, 50, 740, 410)

        val surface = RecordingSurfaces.create(encoderSurface, encoderSize, displaySize, region)

        assertThat(surface.surface).isNotSameInstanceAs(encoderSurface)
        assertThat(surface.surface.isValid).isTrue()
        assertThat(surface.width).isEqualTo(displaySize.width)
        assertThat(surface.height).isEqualTo(displaySize.height)
        surface.release()
        assertThat(surface.surface.isValid).isFalse()
    }
}
