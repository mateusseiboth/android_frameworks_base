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

import org.junit.Assert.assertEquals
import org.junit.Test

class ShortcutHelperPresentationTest {

    @Test
    fun find_dialogDisabled_bottomSheet() {
        assertEquals(
            ShortcutHelperPresentation.BottomSheet,
            ShortcutHelperPresentation.find(
                isDialogEnabled = false,
                screenWidthDp = 1280,
                screenHeightDp = 800,
                isLandscape = true,
            ),
        )
    }

    @Test
    fun find_landscape_wideDialog() {
        assertEquals(
            ShortcutHelperPresentation.Dialog(widthDp = 960),
            ShortcutHelperPresentation.find(
                isDialogEnabled = true,
                screenWidthDp = 1280,
                screenHeightDp = 800,
                isLandscape = true,
            ),
        )
    }

    @Test
    fun find_portrait_narrowerDialog() {
        assertEquals(
            ShortcutHelperPresentation.Dialog(widthDp = 704),
            ShortcutHelperPresentation.find(
                isDialogEnabled = true,
                screenWidthDp = 800,
                screenHeightDp = 1280,
                isLandscape = false,
            ),
        )
    }

    @Test
    fun find_screenNarrowerThanTheDialog_keepsSideMargins() {
        assertEquals(
            ShortcutHelperPresentation.Dialog(widthDp = 852),
            ShortcutHelperPresentation.find(
                isDialogEnabled = true,
                screenWidthDp = 900,
                screenHeightDp = 600,
                isLandscape = true,
            ),
        )
    }

    @Test
    fun find_compactWidth_bottomSheet() {
        assertEquals(
            ShortcutHelperPresentation.BottomSheet,
            ShortcutHelperPresentation.find(
                isDialogEnabled = true,
                screenWidthDp = 599,
                screenHeightDp = 900,
                isLandscape = false,
            ),
        )
    }

    @Test
    fun find_compactHeight_bottomSheet() {
        assertEquals(
            ShortcutHelperPresentation.BottomSheet,
            ShortcutHelperPresentation.find(
                isDialogEnabled = true,
                screenWidthDp = 900,
                screenHeightDp = 479,
                isLandscape = true,
            ),
        )
    }
}
