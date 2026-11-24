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

package com.android.systemui.qs.panels.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.common.shared.model.ContentDescription
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.qs.panels.shared.model.SizedTile
import com.android.systemui.qs.panels.shared.model.SizedTileImpl
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.shared.model.TileCategory
import com.android.systemui.res.R
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@SmallTest
@RunWith(AndroidJUnit4::class)
class ImmediateMouseDragTest : SysuiTestCase() {

    @get:Rule val composeRule = createComposeRule()

    private val dragState = RecordingDragAndDropState()

    @Test
    fun mouseDrag_enabled_startsWithoutLongPress() {
        setImmediateMouseDrag(true)
        setTileContent()

        composeRule.onNodeWithTag(TILE_TAG).performMouseInput {
            press()
            moveBy(Offset(80f, 0f))
        }
        composeRule.waitForIdle()

        assertThat(dragState.startedCount).isEqualTo(1)
    }

    @Test
    fun mouseDrag_disabled_waitsForLongPress() {
        setImmediateMouseDrag(false)
        setTileContent()

        composeRule.onNodeWithTag(TILE_TAG).performMouseInput {
            press()
            moveBy(Offset(80f, 0f))
        }
        composeRule.waitForIdle()

        assertThat(dragState.startedCount).isEqualTo(0)
    }

    @Test
    fun touchSwipe_enabled_waitsForLongPress() {
        setImmediateMouseDrag(true)
        setTileContent()

        composeRule.onNodeWithTag(TILE_TAG).performTouchInput {
            down(center)
            moveBy(Offset(80f, 0f))
        }
        composeRule.waitForIdle()

        assertThat(dragState.startedCount).isEqualTo(0)
    }

    private fun setTileContent() {
        composeRule.setContent {
            // The overridden config lives in the test context's resources.
            CompositionLocalProvider(
                LocalContext provides context,
                LocalResources provides context.resources,
            ) {
                Box(
                    Modifier.size(200.dp)
                        .testTag(TILE_TAG)
                        .dragAndDropTileSource(
                            SizedTileImpl(TestTile, 1),
                            dragState,
                            DragType.Move,
                        ) {}
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun setImmediateMouseDrag(enabled: Boolean) {
        context.orCreateTestableResources.addOverride(
            R.bool.config_qsEditModeImmediateMouseDrag,
            enabled,
        )
    }

    private class RecordingDragAndDropState : DragAndDropState {
        var startedCount = 0
        override val draggedCell: SizedTile<EditTileViewModel>? = null
        override val isDraggedCellRemovable = false
        override val draggedPosition = Offset.Zero
        override var dragInProgress = false
        override val dragType: DragType? = null

        override fun isMoving(tileSpec: TileSpec) = false

        override fun onStarted(cell: SizedTile<EditTileViewModel>, dragType: DragType) {
            startedCount++
            dragInProgress = true
        }

        override fun onTargeting(target: Int, insertAfter: Boolean) {}

        override fun onMoved(offset: Offset) {}

        override fun movedOutOfBounds() {}

        override fun onDrop() {
            dragInProgress = false
        }
    }

    private companion object {
        const val TILE_TAG = "tile"
        val TestTile =
            EditTileViewModel(
                tileSpec = TileSpec.create("tileA"),
                icon = Icon.Resource(android.R.drawable.star_on, ContentDescription.Loaded("A")),
                label = AnnotatedString("tileA"),
                inlinedLabel = null,
                appName = null,
                isCurrent = true,
                isDualTarget = false,
                availableEditActions = emptySet(),
                appIcon = null,
                category = TileCategory.UNKNOWN,
            )
    }
}
