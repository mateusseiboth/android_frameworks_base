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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.LauncherProxyService
import com.android.systemui.SysuiTestCase
import com.android.systemui.res.R
import dagger.Lazy
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@SmallTest
@RunWith(AndroidJUnit4::class)
class DesktopStatusBarPointerInteractorTest : SysuiTestCase() {

    private val launcherProxyService = mock<LauncherProxyService>()

    private fun createInteractor() =
        DesktopStatusBarPointerInteractor(mContext, Lazy { launcherProxyService })

    @Test
    fun emptyAreaTap_closesLauncherFloatingViewsOnItsDisplay() {
        overrideResource(R.bool.config_statusBarTapClosesLauncherPanels, true)

        createInteractor().onEmptyAreaTapped(displayId = 2)

        verify(launcherProxyService).closeLauncherFloatingViews(2)
    }

    @Test
    fun emptyAreaTap_configOff_leavesLauncherAlone() {
        overrideResource(R.bool.config_statusBarTapClosesLauncherPanels, false)

        createInteractor().onEmptyAreaTapped(displayId = 0)

        verify(launcherProxyService, never()).closeLauncherFloatingViews(any())
    }
}
