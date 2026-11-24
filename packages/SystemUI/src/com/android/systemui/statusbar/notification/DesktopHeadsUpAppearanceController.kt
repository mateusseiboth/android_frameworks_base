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

package com.android.systemui.statusbar.notification

import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.shade.ShadeViewController
import com.android.systemui.statusbar.notification.collection.NotificationEntry
import com.android.systemui.statusbar.notification.headsup.HeadsUpManager
import com.android.systemui.statusbar.notification.headsup.OnHeadsUpChangedListener
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayoutController
import java.util.function.BiConsumer
import java.util.function.Consumer
import javax.inject.Inject

/**
 * Heads-up appearance for the desktop status bar, which has no phone status bar view and so no
 * [com.android.systemui.statusbar.phone.HeadsUpAppearanceController]: a heads-up row keeps its
 * header and is fully rounded while it floats over the screen (pinned, animating away, or pulled
 * down by the user), and returns to the list's roundness afterwards.
 */
@SysUISingleton
class DesktopHeadsUpAppearanceController
@Inject
constructor(
    private val headsUpManager: HeadsUpManager,
    private val stackScrollerController: NotificationStackScrollLayoutController,
    private val shadeViewController: ShadeViewController,
    private val notificationRoundnessManager: NotificationRoundnessManager,
) : OnHeadsUpChangedListener {

    private var trackedRow: ExpandableNotificationRow? = null
    private var expandedHeight = 0f

    // The shade reports a null row once the user stops pulling it down from a heads-up; lambda
    // parameters have no null assertion, so the row is read as nullable.
    private val onTrackingHeadsUp =
        BiConsumer<ExpandableNotificationRow, String> { row, _ ->
            setTrackedRow(row as ExpandableNotificationRow?)
        }
    private val onExpandedHeightChanged =
        BiConsumer<Float, Float> { height, _ -> setExpandedHeight(height) }
    private val onHeadsUpAnimatingAway = Consumer<ExpandableNotificationRow> { update(it) }

    private val shadeHeadsUpTracker
        get() = shadeViewController.shadeHeadsUpTracker

    /** Starts following the heads-up rows. */
    fun register() {
        trackedRow = shadeHeadsUpTracker.trackedHeadsUpNotification
        expandedHeight = stackScrollerController.expandedHeight
        headsUpManager.addListener(this)
        shadeHeadsUpTracker.addTrackingHeadsUpListener(onTrackingHeadsUp)
        stackScrollerController.addOnExpandedHeightChangedListener(onExpandedHeightChanged)
        stackScrollerController.addHeadsUpAnimatingAwayListener(onHeadsUpAnimatingAway)
        updateAll()
    }

    /** Stops following the heads-up rows. */
    fun unregister() {
        headsUpManager.removeListener(this)
        shadeHeadsUpTracker.removeTrackingHeadsUpListener(onTrackingHeadsUp)
        stackScrollerController.removeOnExpandedHeightChangedListener(onExpandedHeightChanged)
        stackScrollerController.removeHeadsUpAnimatingAwayListener(onHeadsUpAnimatingAway)
        trackedRow = null
    }

    override fun onHeadsUpPinned(entry: NotificationEntry) = update(entry.row)

    override fun onHeadsUpUnPinned(entry: NotificationEntry) = update(entry.row)

    override fun onHeadsUpStateChanged(entry: NotificationEntry, isHeadsUp: Boolean) =
        update(entry.row)

    private fun setTrackedRow(row: ExpandableNotificationRow?) {
        val previousRow = trackedRow
        trackedRow = row
        update(previousRow)
    }

    private fun setExpandedHeight(height: Float) {
        // Only a change of height counts: the appear fraction alone would loop with the panel.
        if (height == expandedHeight) return
        expandedHeight = height
        updateAll()
    }

    private fun updateAll() = headsUpManager.allEntries.forEach { update(it.row) }

    private fun update(row: ExpandableNotificationRow?) {
        row ?: return
        row.setHeaderVisibleAmount(1f)
        val state = row.headsUpRowState()
        row.apply(headsUpRoundness(state), HEADS_UP)
        row.apply(pulsingRoundness(state), PULSING)
    }

    private fun ExpandableNotificationRow.headsUpRowState() =
        HeadsUpRowState(
            isPinned = isPinned,
            isAnimatingAway = isHeadsUpAnimatingAway,
            isTracked = this == trackedRow,
            isShowingPulsing = showingPulsing(),
            shouldRoundPulsing = notificationRoundnessManager.shouldRoundNotificationPulsing(),
        )

    private fun ExpandableNotificationRow.apply(request: RoundnessRequest, source: SourceType) {
        when (request) {
            RoundnessRequest.ROUND -> requestRoundness(/* top= */ 1f, /* bottom= */ 1f, source)
            RoundnessRequest.RESET -> requestRoundnessReset(source)
            RoundnessRequest.UNCHANGED -> Unit
        }
    }

    private companion object {
        val HEADS_UP = SourceType.from("DesktopHeadsUp")
        val PULSING = SourceType.from("DesktopPulsing")
    }
}
