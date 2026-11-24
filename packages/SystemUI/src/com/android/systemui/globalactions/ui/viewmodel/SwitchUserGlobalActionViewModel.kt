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

import androidx.compose.runtime.getValue
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.globalactions.shared.model.GlobalActionType
import com.android.systemui.res.R
import com.android.systemui.user.domain.interactor.UserLogoutInteractor
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.map

/**
 * ViewModel for the "Switch user" global action: shows the login screen (the headless system user)
 * and keeps the current user signed in. Visible where signing out goes to the login screen.
 */
class SwitchUserGlobalActionViewModel
@AssistedInject
constructor(private val userLogoutInteractor: UserLogoutInteractor) : GlobalActionViewModel() {

    override val key = GlobalActionType.SWITCH_USER

    private val visibleState =
        GlobalActionUiState.Visible(
            key = key,
            icon =
                Icon.Resource(
                    resId = R.drawable.ic_global_actions_switch_user,
                    contentDescription = null,
                ),
            textResId = R.string.global_action_switch_user,
            onClick = { userLogoutInteractor.switchToLoginScreen() },
        )

    override val state: GlobalActionUiState by
        userLogoutInteractor.isSwitchToLoginScreenEnabled
            .map(::toUiState)
            .hydratedStateOf(
                traceName = "switch_user_action_state",
                initialValue = toUiState(userLogoutInteractor.isSwitchToLoginScreenEnabled.value),
            )

    private fun toUiState(isEnabled: Boolean): GlobalActionUiState =
        mapOf(true to visibleState, false to GlobalActionUiState.Hidden(key)).getValue(isEnabled)

    @AssistedFactory
    interface Factory {
        fun create(): SwitchUserGlobalActionViewModel
    }
}
