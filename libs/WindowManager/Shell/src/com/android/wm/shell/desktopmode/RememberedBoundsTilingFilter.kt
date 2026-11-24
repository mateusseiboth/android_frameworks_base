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

package com.android.wm.shell.desktopmode

/**
 * Decides whether a user bounds change of a desktop task is saved as the remembered launch bounds
 * of its app.
 *
 * A window snapped to half of the screen (tiled) has bounds chosen by the tiling layout, not by the
 * user, so when [isTiledExcluded] they are not remembered and a relaunch opens the app at the last
 * free size.
 */
class RememberedBoundsTilingFilter(private val isTiledExcluded: Boolean) {

    /**
     * Returns whether the bounds of [taskId] may be remembered given the tiled tasks of its desk.
     */
    fun shouldRemember(taskId: Int, leftTiledTaskId: Int?, rightTiledTaskId: Int?): Boolean {
        if (!isTiledExcluded) return true
        return taskId != leftTiledTaskId && taskId != rightTiledTaskId
    }
}
