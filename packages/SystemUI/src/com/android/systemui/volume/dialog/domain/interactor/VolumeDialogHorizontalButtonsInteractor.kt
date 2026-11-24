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

package com.android.systemui.volume.dialog.domain.interactor

import android.content.Context
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.res.R
import javax.inject.Inject

/** Decides whether the horizontal volume dialog shows the ringer drawer and the settings button. */
@SysUISingleton
class VolumeDialogHorizontalButtonsInteractor
@Inject
constructor(
    @Application context: Context,
    expandedAudioTileDetailsFeatureInteractor: ExpandedAudioTileDetailsFeatureInteractor,
) {

    /**
     * Whether the dialog is horizontal and leaves out the ringer drawer and the settings button,
     * giving their room to a wider slider.
     */
    val isWithoutButtons: Boolean =
        expandedAudioTileDetailsFeatureInteractor.isEnabled() &&
            context.resources.getBoolean(R.bool.config_volumeDialogHorizontalWithoutButtons)
}
