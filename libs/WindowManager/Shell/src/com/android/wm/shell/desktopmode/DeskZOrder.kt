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

/** Stacking order rules for the windows of a desk. */
object DeskZOrder {

    /**
     * Returns [recordedTopFirst] with its running tasks put in the order the window manager stacks
     * them ([liveTopFirst]). Tasks that are not running keep their slot, and live tasks that are
     * not recorded in the desk are ignored.
     */
    fun mergeWindowOrder(recordedTopFirst: List<Int>, liveTopFirst: List<Int>): List<Int> {
        val recorded = recordedTopFirst.toSet()
        val live = liveTopFirst.distinct().filter { it in recorded }
        if (live.isEmpty()) return recordedTopFirst
        val liveSet = live.toSet()
        val nextLive = live.iterator()
        return recordedTopFirst.map { taskId ->
            when (taskId) {
                in liveSet -> nextLive.next()
                else -> taskId
            }
        }
    }
}
