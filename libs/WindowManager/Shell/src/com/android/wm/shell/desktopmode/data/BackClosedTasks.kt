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

package com.android.wm.shell.desktopmode.data

/**
 * Desktop tasks that back navigation closed and that stay in their desk as minimized windows.
 *
 * The task vanishes from the window hierarchy once its last activity finishes. That removal is
 * skipped once, so the window keeps its minimized entry and a restore relaunches it from recents.
 */
class BackClosedTasks {
    private val taskIds = LinkedHashSet<Int>()

    /** Marks [taskId] as closed by back navigation. */
    fun add(taskId: Int) {
        taskIds += taskId
    }

    /** Stops tracking [taskId], e.g. when it is restored or removed for good. */
    fun remove(taskId: Int) {
        taskIds -= taskId
    }

    /** Returns whether [taskId] is tracked. */
    fun contains(taskId: Int): Boolean = taskId in taskIds

    /** Returns true once for a tracked [taskId], whose removal must then be skipped. */
    fun takeRemovalSkip(taskId: Int): Boolean = taskIds.remove(taskId)

    override fun toString(): String = taskIds.joinToString(prefix = "[", postfix = "]")
}
