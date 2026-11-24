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

package com.android.systemui.statusbar.phone

import com.android.systemui.statusbar.phone.DialogTaskbarStash.ShowingDialog
import org.junit.Assert.assertEquals
import org.junit.Test

class DialogTaskbarStashTest {

    @Test
    fun noDialogs_stashesNowhere() {
        assertEquals(emptySet<Int>(), DialogTaskbarStash.displaysToStash(emptyList()))
    }

    @Test
    fun dialogThatAsks_stashesItsDisplay() {
        val dialogs = listOf(ShowingDialog(displayId = 2, asksToStashTaskbar = true))

        assertEquals(setOf(2), DialogTaskbarStash.displaysToStash(dialogs))
    }

    @Test
    fun dialogThatDoesNotAsk_keepsTheTaskbar() {
        val dialogs = listOf(ShowingDialog(displayId = 0, asksToStashTaskbar = false))

        assertEquals(emptySet<Int>(), DialogTaskbarStash.displaysToStash(dialogs))
    }

    @Test
    fun displayWithBothKinds_stashes() {
        val dialogs =
            listOf(
                ShowingDialog(displayId = 0, asksToStashTaskbar = false),
                ShowingDialog(displayId = 0, asksToStashTaskbar = true),
                ShowingDialog(displayId = 1, asksToStashTaskbar = false),
            )

        assertEquals(setOf(0), DialogTaskbarStash.displaysToStash(dialogs))
    }
}
