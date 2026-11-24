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

package com.android.systemui.globalactions.ui.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.globalactions.shared.model.GlobalActionType
import com.android.systemui.kosmos.Kosmos
import com.android.systemui.kosmos.runTest
import com.android.systemui.kosmos.testScope
import com.android.systemui.lifecycle.activateIn
import com.android.systemui.res.R
import com.android.systemui.statusbar.policy.data.repository.fakeUserSetupRepository
import com.android.systemui.testKosmosNew
import com.android.systemui.user.data.repository.fakeUserRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class SwitchUserGlobalActionViewModelTest : SysuiTestCase() {

    private val kosmos = testKosmosNew()
    private val Kosmos.underTest by
        Kosmos.Fixture {
            switchUserGlobalActionViewModelFactory.create().apply { activateIn(testScope) }
        }

    @Before
    fun setup() {
        kosmos.fakeUserRepository.setPolicyManagerLogoutEnabled(false)
        kosmos.fakeUserRepository.setUserManagerLogoutEnabled(false)
    }

    @Test
    fun state_withALoginScreen_isVisibleAndSwitchesToIt() =
        kosmos.runTest {
            fakeUserRepository.setUserManagerLogoutEnabled(true)

            val state = underTest.state
            assertThat(state).isInstanceOf(GlobalActionUiState.Visible::class.java)
            val visibleState = state as GlobalActionUiState.Visible
            assertThat(visibleState.key).isEqualTo(GlobalActionType.SWITCH_USER)
            assertThat(visibleState.textResId).isEqualTo(R.string.global_action_switch_user)
            assertThat((visibleState.icon as Icon.Resource).resId)
                .isEqualTo(R.drawable.ic_global_actions_switch_user)

            visibleState.onClick()

            assertThat(fakeUserRepository.switchToLoginScreenCallCount).isEqualTo(1)
            assertThat(fakeUserRepository.logOutWithUserManagerCallCount).isEqualTo(0)
        }

    @Test
    fun state_withoutALoginScreen_isHidden() =
        kosmos.runTest {
            fakeUserRepository.setPolicyManagerLogoutEnabled(true)

            val state = underTest.state
            assertThat(state).isInstanceOf(GlobalActionUiState.Hidden::class.java)
            assertThat(state.key).isEqualTo(GlobalActionType.SWITCH_USER)
        }

    @Test
    fun state_whileTheUserIsInSetup_isHidden() =
        kosmos.runTest {
            fakeUserRepository.setUserManagerLogoutEnabled(true)
            fakeUserSetupRepository.setUserSetUp(false)

            assertThat(underTest.state).isInstanceOf(GlobalActionUiState.Hidden::class.java)
        }
}
