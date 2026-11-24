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

package com.android.systemui.volume.dialog.ui.binder

import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeDialogHorizontalSliderSizeTest {

    @Test
    fun find_withButtons_leavesRoomForTheRingerAndSettings() {
        assertEquals(
            VolumeDialogHorizontalSliderSize.WithButtons,
            VolumeDialogHorizontalSliderSize.find(
                isCaptionsToggleVisible = false,
                isWithoutButtons = false,
            ),
        )
    }

    @Test
    fun find_withoutButtons_takesTheirRoom() {
        assertEquals(
            VolumeDialogHorizontalSliderSize.WithoutButtons,
            VolumeDialogHorizontalSliderSize.find(
                isCaptionsToggleVisible = false,
                isWithoutButtons = true,
            ),
        )
    }

    @Test
    fun find_captionsToggleVisible_keepsItsRoomWithButtons() {
        assertEquals(
            VolumeDialogHorizontalSliderSize.WithCaptions,
            VolumeDialogHorizontalSliderSize.find(
                isCaptionsToggleVisible = true,
                isWithoutButtons = false,
            ),
        )
    }

    @Test
    fun find_captionsToggleVisible_keepsItsRoomWithoutButtons() {
        assertEquals(
            VolumeDialogHorizontalSliderSize.WithCaptions,
            VolumeDialogHorizontalSliderSize.find(
                isCaptionsToggleVisible = true,
                isWithoutButtons = true,
            ),
        )
    }
}
