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

package com.android.systemui.statusbar.quickactions.domain.startable

import android.content.Context
import com.android.systemui.CoreStartable
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.res.R
import com.android.systemui.statusbar.CommandQueue
import com.android.systemui.statusbar.quickactions.domain.interactor.QuickActionsInteractor
import com.android.systemui.statusbar.quickactions.popups.StatusBarPopupChips
import javax.inject.Inject

/**
 * Closes the open status bar quick action popup when panels are collapsed
 * ([android.app.StatusBarManager.collapsePanels], e.g. on a click on the taskbar), as the shade
 * closes, when config_collapsePanelsClosesQuickActions is set.
 */
@SysUISingleton
class QuickActionsCollapseStartable
@Inject
constructor(
    @Application private val context: Context,
    private val commandQueue: CommandQueue,
    private val quickActionsInteractor: QuickActionsInteractor,
) : CoreStartable, CommandQueue.Callbacks {

    override fun start() {
        if (!StatusBarPopupChips.isEnabled) return
        if (!context.resources.getBoolean(R.bool.config_collapsePanelsClosesQuickActions)) return
        commandQueue.addCallback(this)
    }

    override fun animateCollapsePanels(flags: Int, force: Boolean) {
        if (quickActionsInteractor.activePanel == null) return
        quickActionsInteractor.close()
    }
}
