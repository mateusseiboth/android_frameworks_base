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

package com.android.systemui.screenrecord.shared.model

import android.graphics.Rect
import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.screenrecord.ScreenRecordingAudioSource
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class ScreenRecordingParametersTest : SysuiTestCase() {

    @Test
    fun cropRegion_defaultsToNull() {
        assertThat(parametersOf(cropRegion = null).cropRegion).isNull()
    }

    @Test
    fun parcel_keepsTheCropRegion() {
        val parameters = parametersOf(cropRegion = Rect(10, 20, 330, 260))

        assertThat(roundTrip(parameters)).isEqualTo(parameters)
    }

    @Test
    fun parcel_keepsAMissingCropRegion() {
        val parameters = parametersOf(cropRegion = null)

        assertThat(roundTrip(parameters).cropRegion).isNull()
    }

    private fun parametersOf(cropRegion: Rect?) =
        ScreenRecordingParameters(
            captureTarget = null,
            audioSource = ScreenRecordingAudioSource.MIC,
            displayId = 0,
            shouldShowTaps = true,
            cropRegion = cropRegion,
        )

    private fun roundTrip(parameters: ScreenRecordingParameters): ScreenRecordingParameters {
        val parcel = Parcel.obtain()
        try {
            parameters.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            return ScreenRecordingParameters.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}
