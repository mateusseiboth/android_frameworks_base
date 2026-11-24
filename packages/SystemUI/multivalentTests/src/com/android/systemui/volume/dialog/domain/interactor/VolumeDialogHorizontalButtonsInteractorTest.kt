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

import android.platform.test.annotations.DisableFlags
import android.platform.test.annotations.EnableFlags
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.Flags
import com.android.systemui.SysuiTestCase
import com.android.systemui.flags.EnableSceneContainer
import com.android.systemui.kosmos.runTest
import com.android.systemui.res.R
import com.android.systemui.testKosmos
import com.android.systemui.volume.dialog.ringer.ui.viewmodel.RingerViewModelState
import com.android.systemui.volume.dialog.ringer.ui.viewmodel.volumeDialogRingerDrawerViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
@EnableSceneContainer
@EnableFlags(Flags.FLAG_QS_TILE_DETAILED_VIEW, Flags.FLAG_EXPANDED_AUDIO_DETAILED_VIEW)
class VolumeDialogHorizontalButtonsInteractorTest : SysuiTestCase() {

    private val kosmos = testKosmos()

    @Test
    fun isWithoutButtons_horizontalAndConfigOn_true() =
        kosmos.runTest {
            setWithoutButtons(true)

            assertThat(volumeDialogHorizontalButtonsInteractor.isWithoutButtons).isTrue()
        }

    @Test
    fun isWithoutButtons_configOff_false() =
        kosmos.runTest {
            setWithoutButtons(false)

            assertThat(volumeDialogHorizontalButtonsInteractor.isWithoutButtons).isFalse()
        }

    @Test
    @DisableFlags(Flags.FLAG_EXPANDED_AUDIO_DETAILED_VIEW)
    fun isWithoutButtons_verticalDialog_false() =
        kosmos.runTest {
            setWithoutButtons(true)

            assertThat(volumeDialogHorizontalButtonsInteractor.isWithoutButtons).isFalse()
        }

    @Test
    fun ringerDrawer_withoutButtons_unavailable() =
        kosmos.runTest {
            setWithoutButtons(true)

            assertThat(volumeDialogRingerDrawerViewModel.ringerViewModel.first())
                .isEqualTo(RingerViewModelState.Unavailable)
        }

    private fun setWithoutButtons(withoutButtons: Boolean) {
        context.orCreateTestableResources.addOverride(
            R.bool.config_volumeDialogHorizontalWithoutButtons,
            withoutButtons,
        )
    }
}
