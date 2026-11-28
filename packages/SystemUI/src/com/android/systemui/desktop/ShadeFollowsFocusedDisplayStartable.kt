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

package com.android.systemui.desktop

import android.content.res.Resources
import com.android.systemui.CoreStartable
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.display.data.repository.FocusedDisplayRepository
import com.android.systemui.res.R
import com.android.systemui.shade.display.StatusBarTouchShadeDisplayPolicy
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * With several monitors, the shade (its popups and the heads-up cards) goes to the display the user
 * works on: the one that gets the focus, as well as the one whose status bar was touched
 * ([StatusBarTouchShadeDisplayPolicy]). On when config_shadeFollowsFocusedDisplay is set.
 */
@SysUISingleton
class ShadeFollowsFocusedDisplayStartable
@Inject
constructor(
    @Background private val backgroundScope: CoroutineScope,
    @Main private val resources: Resources,
    private val focusedDisplayRepository: FocusedDisplayRepository,
    private val statusBarTouchPolicy: StatusBarTouchShadeDisplayPolicy,
) : CoreStartable {

    override fun start() {
        if (!resources.getBoolean(R.bool.config_shadeFollowsFocusedDisplay)) return
        backgroundScope.launch {
            focusedDisplayRepository.focusedDisplayId.collect {
                statusBarTouchPolicy.updateShadeDisplayIfNeeded(it)
            }
        }
    }
}
