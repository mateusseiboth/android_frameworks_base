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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [DesktopScrimTransitionTracker].
 *
 * Usage: atest WMShellUnitTests:DesktopScrimTransitionTrackerTest
 */
class DesktopScrimTransitionTrackerTest {
    private val tracker = DesktopScrimTransitionTracker<String>()

    @Test
    fun untrackedTransition_doesNotUpdate() {
        assertFalse(tracker.onFinished("t1", aborted = false))
    }

    @Test
    fun trackedTransition_updatesWhenFinished() {
        tracker.track("t1", updateWhenAborted = false)

        assertTrue(tracker.onFinished("t1", aborted = false))
    }

    @Test
    fun trackedTransition_abortedDoesNotUpdate() {
        tracker.track("t1", updateWhenAborted = false)

        assertFalse(tracker.onFinished("t1", aborted = true))
    }

    @Test
    fun trackedTransitionUpdatingWhenAborted_abortedUpdates() {
        tracker.track("t1", updateWhenAborted = true)

        assertTrue(tracker.onFinished("t1", aborted = true))
    }

    @Test
    fun finishedTransition_isForgotten() {
        tracker.track("t1", updateWhenAborted = true)
        tracker.onFinished("t1", aborted = false)

        assertFalse(tracker.onFinished("t1", aborted = false))
    }

    @Test
    fun mergedTransition_updatesWhenTheTransitionItJoinedFinishes() {
        tracker.track("merged", updateWhenAborted = false)

        tracker.onMerged(merged = "merged", playing = "playing")

        assertFalse(tracker.onFinished("merged", aborted = false))
        assertTrue(tracker.onFinished("playing", aborted = false))
    }

    @Test
    fun mergedTransitionUpdatingWhenAborted_keepsItsKind() {
        tracker.track("merged", updateWhenAborted = true)

        tracker.onMerged(merged = "merged", playing = "playing")

        assertTrue(tracker.onFinished("playing", aborted = true))
    }

    @Test
    fun mergeOfUntrackedTransition_doesNotTrackThePlayingOne() {
        tracker.onMerged(merged = "other", playing = "playing")

        assertFalse(tracker.onFinished("playing", aborted = false))
    }
}
