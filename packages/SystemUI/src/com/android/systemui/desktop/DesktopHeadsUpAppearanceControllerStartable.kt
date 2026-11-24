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
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.desktop.domain.interactor.DesktopInteractor
import com.android.systemui.res.R
import com.android.systemui.statusbar.core.StatusBarForDesktop
import com.android.systemui.statusbar.notification.DesktopHeadsUpAppearanceController
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Runs the [DesktopHeadsUpAppearanceController] while the desktop status bar is in use, when
 * config_desktopHeadsUpAppearance is set.
 */
@SysUISingleton
class DesktopHeadsUpAppearanceControllerStartable
@Inject
constructor(
    @Application private val applicationScope: CoroutineScope,
    @Main private val resources: Resources,
    private val desktopInteractor: DesktopInteractor,
    private val controllerProvider: Provider<DesktopHeadsUpAppearanceController>,
) : CoreStartable {

    override fun start() {
        if (!StatusBarForDesktop.isEnabled) return
        if (!resources.getBoolean(R.bool.config_desktopHeadsUpAppearance)) return
        applicationScope.launch {
            desktopInteractor.useDesktopStatusBar.collectLatest { useDesktopStatusBar ->
                if (!useDesktopStatusBar) return@collectLatest
                followHeadsUps()
            }
        }
    }

    private suspend fun followHeadsUps() {
        val controller = controllerProvider.get()
        controller.register()
        try {
            awaitCancellation()
        } finally {
            controller.unregister()
        }
    }
}
