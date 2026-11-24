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

package com.android.systemui.statusbar.notification.row

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DismissButtonVisibilityTest {

    @Test
    fun hoveredNotification_showsTheButton() {
        assertTrue(isDismissButtonShown(state(isHovered = true)))
    }

    @Test
    fun notificationWithoutHoverOrFocus_hidesTheButton() {
        assertFalse(isDismissButtonShown(state()))
    }

    @Test
    fun keyboardFocusWithin_showsTheButtonWhenReachableByFocus() {
        assertTrue(isDismissButtonShown(state(hasFocusWithin = true)))
    }

    @Test
    fun accessibilityFocusWithin_showsTheButtonWhenReachableByFocus() {
        assertTrue(isDismissButtonShown(state(hasAccessibilityFocusWithin = true)))
    }

    @Test
    fun focusWithin_keepsTheButtonHiddenWhenNotReachableByFocus() {
        val state =
            state(
                isReachableByFocus = false,
                hasFocusWithin = true,
                hasAccessibilityFocusWithin = true,
            )

        assertFalse(isDismissButtonShown(state))
    }

    @Test
    fun notificationThatCannotBeDismissed_hidesTheButton() {
        val state = state(isDismissible = false, isHovered = true, hasFocusWithin = true)

        assertFalse(isDismissButtonShown(state))
    }

    private fun state(
        isDismissible: Boolean = true,
        isHovered: Boolean = false,
        isReachableByFocus: Boolean = true,
        hasFocusWithin: Boolean = false,
        hasAccessibilityFocusWithin: Boolean = false,
    ) =
        DismissButtonState(
            isDismissible = isDismissible,
            isHovered = isHovered,
            isReachableByFocus = isReachableByFocus,
            hasFocusWithin = hasFocusWithin,
            hasAccessibilityFocusWithin = hasAccessibilityFocusWithin,
        )
}
