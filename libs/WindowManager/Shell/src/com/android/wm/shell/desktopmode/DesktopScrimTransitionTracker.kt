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
 * Remembers the transitions after which the desktop scrim has to be computed again, following
 * them through merges, and tells when one of them is over.
 *
 * @param T the transition token type.
 */
class DesktopScrimTransitionTracker<T : Any> {
    private val updatedWhenCompleted = mutableSetOf<T>()
    private val updatedWhenAborted = mutableSetOf<T>()

    /**
     * Tracks [transition]. When [updateWhenAborted] is false, an aborted transition changes
     * nothing on screen and does not ask for a new computation.
     */
    fun track(transition: T, updateWhenAborted: Boolean) {
        setFor(updateWhenAborted).add(transition)
    }

    /** Moves the tracking of [merged] to the transition [playing] it was merged into. */
    fun onMerged(merged: T, playing: T) {
        moveIfTracked(updatedWhenCompleted, merged, playing)
        moveIfTracked(updatedWhenAborted, merged, playing)
    }

    /**
     * Forgets [transition] and returns whether the scrim has to be computed again because of it.
     */
    fun onFinished(transition: T, aborted: Boolean): Boolean {
        val isUpdatedWhenAborted = updatedWhenAborted.remove(transition)
        val isUpdatedWhenCompleted = updatedWhenCompleted.remove(transition)
        return isUpdatedWhenAborted || (isUpdatedWhenCompleted && !aborted)
    }

    private fun setFor(updateWhenAborted: Boolean): MutableSet<T> =
        when (updateWhenAborted) {
            true -> updatedWhenAborted
            false -> updatedWhenCompleted
        }

    private fun moveIfTracked(transitions: MutableSet<T>, from: T, to: T) {
        if (!transitions.remove(from)) return
        transitions.add(to)
    }
}
