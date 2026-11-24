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

import android.graphics.RectF
import android.platform.test.annotations.EnableFlags
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.kosmos.Kosmos
import com.android.systemui.kosmos.runTest
import com.android.systemui.res.R
import com.android.systemui.scene.domain.interactor.sceneInteractor
import com.android.systemui.scene.shared.model.Overlays
import com.android.systemui.statusbar.getCommandQueueCallback
import com.android.systemui.statusbar.mockCommandQueue
import com.android.systemui.statusbar.quickactions.domain.interactor.quickActionsInteractor
import com.android.systemui.statusbar.quickactions.media.ui.viewmodel.mediaControlPopupViewModelFactory
import com.android.systemui.statusbar.quickactions.popups.StatusBarPopupChips
import com.android.systemui.statusbar.quickactions.shared.model.QuickActionChipId
import com.android.systemui.statusbar.quickactions.shared.model.QuickActionPanelModel
import com.android.systemui.testKosmosNew
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@SmallTest
@EnableFlags(StatusBarPopupChips.FLAG_NAME)
@RunWith(AndroidJUnit4::class)
class QuickActionsCollapseStartableTest : SysuiTestCase() {
    private val kosmos = testKosmosNew()

    private val panel =
        QuickActionPanelModel(
            chipId = QuickActionChipId.MediaControl,
            anchorBounds = RectF(),
            panelContentViewModelFactory = kosmos.mediaControlPopupViewModelFactory,
        )

    private fun Kosmos.createStartable() =
        QuickActionsCollapseStartable(mContext, mockCommandQueue, quickActionsInteractor)

    @Test
    fun collapsePanels_closesOpenPopup() =
        kosmos.runTest {
            overrideResource(R.bool.config_collapsePanelsClosesQuickActions, true)
            createStartable().start()
            quickActionsInteractor.toggle(panel)

            getCommandQueueCallback().animateCollapsePanels(0, false)

            assertThat(quickActionsInteractor.activePanel).isNull()
            assertThat(sceneInteractor.transitionState.currentOverlays)
                .doesNotContain(Overlays.QuickActions)
        }

    @Test
    fun configOff_doesNotListenToCollapsePanels() =
        kosmos.runTest {
            overrideResource(R.bool.config_collapsePanelsClosesQuickActions, false)
            createStartable().start()

            verify(mockCommandQueue, never()).addCallback(any())
        }
}
