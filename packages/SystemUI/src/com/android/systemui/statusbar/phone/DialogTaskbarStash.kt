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

/** Which displays stash their taskbar for the SystemUI dialogs showing on them. */
object DialogTaskbarStash {

    /** A dialog that is showing, and whether it asks the taskbar of its display to stash. */
    data class ShowingDialog(val displayId: Int, val asksToStashTaskbar: Boolean)

    /** The displays with at least one showing dialog that asks the taskbar to stash. */
    @JvmStatic
    fun displaysToStash(dialogs: Collection<ShowingDialog>): Set<Int> =
        dialogs.filter { it.asksToStashTaskbar }.mapTo(HashSet()) { it.displayId }
}
