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

package com.android.systemui.statusbar.pipeline.shared.ui.model

import android.graphics.drawable.Icon
import android.os.UserHandle
import com.android.systemui.statusbar.notification.icon.domain.interactor.ActiveNotificationIconModel

/** One icon of the notifications chip: [icon] loads as [userId], the user who got it. */
data class StartNotificationIcon(val notifKey: String, val icon: Icon, val userId: Int)

/**
 * What the notifications chip next to the desktop status bar's clock shows: the status bar icons
 * of the first notifications, in the order of the notification list, and how many more there are.
 */
data class StartNotificationIconsModel(
    val icons: List<StartNotificationIcon>,
    val overflowCount: Int,
) {

    val hasIcons: Boolean
        get() = icons.isNotEmpty()

    companion object {
        /** Icons the chip shows before it counts the rest. */
        const val MAX_ICONS = 4

        val Empty = StartNotificationIconsModel(icons = emptyList(), overflowCount = 0)

        /**
         * The chip for [notifs], the status bar notifications in list order. [userIdOf] gives the
         * user of a notification key; keys it does not know load as the system user.
         */
        @JvmStatic
        fun from(
            notifs: Collection<ActiveNotificationIconModel>,
            userIdOf: (String) -> Int? = { null },
            maxIcons: Int = MAX_ICONS,
        ): StartNotificationIconsModel {
            val icons =
                notifs.mapNotNull { notif ->
                    notif.statusBarIcon?.let { icon ->
                        StartNotificationIcon(
                            notifKey = notif.notifKey,
                            icon = icon,
                            userId = userIdOf(notif.notifKey) ?: UserHandle.USER_SYSTEM,
                        )
                    }
                }
            return StartNotificationIconsModel(
                icons = icons.take(maxIcons),
                overflowCount = (icons.size - maxIcons).coerceAtLeast(0),
            )
        }
    }
}
