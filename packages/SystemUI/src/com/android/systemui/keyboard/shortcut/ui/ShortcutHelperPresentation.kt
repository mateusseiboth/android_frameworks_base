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

package com.android.systemui.keyboard.shortcut.ui

/** How the keyboard shortcut helper opens on a display. */
sealed interface ShortcutHelperPresentation {

    /** A sheet that slides up from the bottom of the screen. */
    data object BottomSheet : ShortcutHelperPresentation

    /** A dialog with the categories in a sidebar, [widthDp] wide. */
    data class Dialog(val widthDp: Int) : ShortcutHelperPresentation

    companion object {
        // Below these sizes the window is compact and only the bottom sheet fits.
        private const val COMPACT_WIDTH_DP = 600
        private const val COMPACT_HEIGHT_DP = 480

        private const val LANDSCAPE_WIDTH_DP = 960
        private const val PORTRAIT_WIDTH_DP = 704
        private const val SIDE_MARGIN_DP = 24

        fun find(
            isDialogEnabled: Boolean,
            screenWidthDp: Int,
            screenHeightDp: Int,
            isLandscape: Boolean,
        ): ShortcutHelperPresentation {
            if (!isDialogEnabled) return BottomSheet
            if (screenWidthDp < COMPACT_WIDTH_DP || screenHeightDp < COMPACT_HEIGHT_DP) {
                return BottomSheet
            }
            val preferredWidthDp =
                when (isLandscape) {
                    true -> LANDSCAPE_WIDTH_DP
                    false -> PORTRAIT_WIDTH_DP
                }
            return Dialog(minOf(preferredWidthDp, screenWidthDp - 2 * SIDE_MARGIN_DP))
        }
    }
}
