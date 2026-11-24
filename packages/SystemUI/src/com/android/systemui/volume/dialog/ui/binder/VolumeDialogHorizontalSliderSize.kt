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

/** Which side margins and maximum width the main slider of the horizontal volume dialog takes. */
enum class VolumeDialogHorizontalSliderSize {
    /** The ringer drawer before the slider and the settings button after it. */
    WithButtons,
    /** The captions toggle after the slider, which takes more room than the settings button. */
    WithCaptions,
    /** Neither the ringer drawer nor the settings button: the slider takes their room. */
    WithoutButtons;

    companion object {
        fun find(
            isCaptionsToggleVisible: Boolean,
            isWithoutButtons: Boolean,
        ): VolumeDialogHorizontalSliderSize =
            when {
                isCaptionsToggleVisible -> WithCaptions
                isWithoutButtons -> WithoutButtons
                else -> WithButtons
            }
    }
}
