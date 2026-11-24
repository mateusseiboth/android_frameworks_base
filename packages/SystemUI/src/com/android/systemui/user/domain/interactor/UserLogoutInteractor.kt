/*
 * Copyright (C) 2024 The Android Open Source Project
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
 *
 */

package com.android.systemui.user.domain.interactor

import com.android.app.tracing.coroutines.launchTraced as launch
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.statusbar.policy.domain.interactor.UserSetupInteractor
import com.android.systemui.user.data.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Encapsulates business logic to for the logout.
 *
 * A user still in setup finishes it before signing out or leaving for the login screen, so a
 * half set up user is not left behind.
 */
@SysUISingleton
class UserLogoutInteractor
@Inject
constructor(
    private val userRepository: UserRepository,
    userSetupInteractor: UserSetupInteractor,
    @Application private val applicationScope: CoroutineScope,
) {
    private val isUserSetUp = userSetupInteractor.isUserSetUp

    val isLogoutEnabled: StateFlow<Boolean> =
        combine(
                userRepository.isPolicyManagerLogoutEnabled,
                userRepository.isUserManagerLogoutEnabled,
                isUserSetUp,
            ) { policy, userManager, setUp ->
                setUp && (policy || userManager)
            }
            .stateIn(applicationScope, SharingStarted.Eagerly, false)

    val isLogoutToSystemUserEnabled: StateFlow<Boolean> =
        combine(userRepository.isUserManagerLogoutEnabled, isUserSetUp, Boolean::and)
            .stateIn(applicationScope, SharingStarted.Eagerly, false)

    /**
     * Whether the user can go to the login screen and keep running: possible wherever signing out
     * goes to the login screen (the headless system user).
     */
    val isSwitchToLoginScreenEnabled: StateFlow<Boolean> = isLogoutToSystemUserEnabled

    val isPolicyManagerLogoutEnabled: StateFlow<Boolean> =
        userRepository.isPolicyManagerLogoutEnabled

    suspend fun logOutToSystemUser() {
        if (isLogoutToSystemUserEnabled.value) {
            userRepository.logOutWithUserManager()
        }
    }

    fun logOut() {
        if (!isLogoutEnabled.value) return
        applicationScope.launch {
            if (userRepository.isPolicyManagerLogoutEnabled.value) {
                userRepository.logOutWithPolicyManager()
            } else if (userRepository.isUserManagerLogoutEnabled.value) {
                userRepository.logOutWithUserManager()
            }
        }
    }

    /** Shows the login screen without signing the user out. */
    fun switchToLoginScreen() {
        if (!isSwitchToLoginScreenEnabled.value) return
        applicationScope.launch { userRepository.switchToLoginScreen() }
    }
}
