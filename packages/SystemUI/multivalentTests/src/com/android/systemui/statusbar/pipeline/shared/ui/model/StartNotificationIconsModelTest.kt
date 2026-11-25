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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.res.R
import com.android.systemui.statusbar.notification.icon.domain.interactor.ActiveNotificationIconModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class StartNotificationIconsModelTest : SysuiTestCase() {

    @Test
    fun from_noNotifications_isEmpty() {
        val model = StartNotificationIconsModel.from(emptyList())

        assertThat(model).isEqualTo(StartNotificationIconsModel.Empty)
        assertThat(model.hasIcons).isFalse()
    }

    @Test
    fun from_fewNotifications_showsEveryIconInOrder() {
        val first = iconOf(R.drawable.ic_notification_bell)
        val second = iconOf(R.drawable.ic_notification_bell_unread_base)

        val model = StartNotificationIconsModel.from(listOf(notif("a", first), notif("b", second)))

        assertThat(model.icons.map { it.icon }).containsExactly(first, second).inOrder()
        assertThat(model.overflowCount).isEqualTo(0)
        assertThat(model.hasIcons).isTrue()
    }

    @Test
    fun from_manyNotifications_keepsTheFirstIconsAndCountsTheRest() {
        val notifs = (1..6).map { notif("n$it", iconOf(R.drawable.ic_notification_bell)) }

        val model = StartNotificationIconsModel.from(notifs, maxIcons = 4)

        assertThat(model.icons).hasSize(4)
        assertThat(model.overflowCount).isEqualTo(2)
    }

    @Test
    fun from_notificationsWithoutIcon_areLeftOut() {
        val icon = iconOf(R.drawable.ic_notification_bell)

        val model = StartNotificationIconsModel.from(listOf(notif("a", null), notif("b", icon)))

        assertThat(model.icons.map { it.icon }).containsExactly(icon)
        assertThat(model.overflowCount).isEqualTo(0)
    }

    @Test
    fun from_iconsLoadAsTheUserOfTheirNotification() {
        val icon = iconOf(R.drawable.ic_notification_bell)

        val model =
            StartNotificationIconsModel.from(
                listOf(notif("work", icon), notif("unknown", icon)),
                userIdOf = { key -> 10.takeIf { key == "work" } },
            )

        assertThat(model.icons.map { it.userId })
            .containsExactly(10, UserHandle.USER_SYSTEM)
            .inOrder()
    }

    private fun iconOf(resId: Int): Icon = Icon.createWithResource(context, resId)

    private fun notif(key: String, icon: Icon?) =
        ActiveNotificationIconModel(
            notifKey = key,
            groupKey = key,
            shelfIcon = icon,
            statusBarIcon = icon,
            aodIcon = icon,
            isAmbient = false,
        )
}
