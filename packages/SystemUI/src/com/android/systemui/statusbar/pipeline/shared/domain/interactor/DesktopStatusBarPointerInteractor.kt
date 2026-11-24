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

package com.android.systemui.statusbar.pipeline.shared.domain.interactor

import android.content.Context
import com.android.systemui.LauncherProxyService
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.res.R
import dagger.Lazy
import javax.inject.Inject

/** Passes pointer input on the desktop status bar to the components that react to it. */
@SysUISingleton
class DesktopStatusBarPointerInteractor
@Inject
constructor(
    @Application private val context: Context,
    private val launcherProxyService: Lazy<LauncherProxyService>,
) {

    /**
     * A tap on an empty part of the status bar on [displayId] closes the taskbar's All Apps and
     * the launcher's floating views there, when config_statusBarTapClosesLauncherPanels is set.
     */
    fun onEmptyAreaTapped(displayId: Int) {
        if (!context.resources.getBoolean(R.bool.config_statusBarTapClosesLauncherPanels)) return
        launcherProxyService.get().closeLauncherFloatingViews(displayId)
    }
}
