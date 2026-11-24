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
 * Remembers on which half of the screen a window was tiled when it got minimized, so restoring it
 * puts it back into that half while the half is still free.
 */
class MinimizedTiledTasks {
    /** Half of the screen a task was tiled to. */
    enum class TiledSide {
        LEFT,
        RIGHT,
    }

    private val sideByTaskId = LinkedHashMap<Int, TiledSide>()

    /** Records the side of [taskId] if it is one of the tiled tasks of its desk. */
    fun onMinimized(taskId: Int, leftTiledTaskId: Int?, rightTiledTaskId: Int?) {
        val side =
            when (taskId) {
                leftTiledTaskId -> TiledSide.LEFT
                rightTiledTaskId -> TiledSide.RIGHT
                else -> return
            }
        sideByTaskId[taskId] = side
    }

    /**
     * Returns, once, the side to tile [taskId] back into when it is restored, or null when it was
     * not tiled or another task now holds that side.
     */
    fun takeSideToRestore(taskId: Int, leftTiledTaskId: Int?, rightTiledTaskId: Int?): TiledSide? {
        val side = sideByTaskId.remove(taskId) ?: return null
        val holder =
            when (side) {
                TiledSide.LEFT -> leftTiledTaskId
                TiledSide.RIGHT -> rightTiledTaskId
            }
        return side.takeIf { holder == null || holder == taskId }
    }

    /** Returns whether a side is remembered for [taskId]. */
    fun contains(taskId: Int): Boolean = taskId in sideByTaskId

    /** Forgets [taskId], e.g. when it leaves its desk. */
    fun remove(taskId: Int) {
        sideByTaskId.remove(taskId)
    }

    override fun toString(): String = sideByTaskId.toString()
}
