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

@file:JvmName("DismissButtonVisibility")

package com.android.systemui.statusbar.notification.row

/** What decides whether a notification shows its dismiss button. */
data class DismissButtonState(
    val isDismissible: Boolean,
    val isHovered: Boolean,
    /** Whether the button can be reached by keyboard focus and accessibility services. */
    val isReachableByFocus: Boolean,
    /** Whether the notification or one of its views has keyboard focus. */
    val hasFocusWithin: Boolean,
    /** Whether the notification or one of its views has accessibility focus. */
    val hasAccessibilityFocusWithin: Boolean,
)

/**
 * A dismissible notification shows its dismiss button under the pointer and, when the button is
 * reachable by focus, while the keyboard or an accessibility service is on the notification.
 */
fun isDismissButtonShown(state: DismissButtonState): Boolean {
    if (!state.isDismissible) return false
    if (state.isHovered) return true
    if (!state.isReachableByFocus) return false
    return state.hasFocusWithin || state.hasAccessibilityFocusWithin
}
