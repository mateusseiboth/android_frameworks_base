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

package com.android.systemui.keyboard.shortcut.ui.composable

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.systemui.res.R

/** Paddings and sizes of the shortcut helper's dual pane, set by how the helper opens. */
sealed interface ShortcutHelperDualPaneLayout {

    /** Space between the title row and the panes. */
    val titleSpacerHeight: Dp

    /** Padding around the title row and the panes. */
    fun Modifier.content(): Modifier

    fun Modifier.titleRow(): Modifier

    /** The categories sidebar on the start side. */
    @Composable fun Modifier.sidebar(): Modifier

    /** The shortcuts of the selected category on the end side. */
    fun Modifier.endPanel(): Modifier

    /** Actions after the customization buttons at the end of the title row. */
    @Composable fun RowScope.TrailingTitleActions(onCloseClicked: () -> Unit)

    /** A sheet that slides up from the bottom; it closes by dragging it down. */
    data object BottomSheet : ShortcutHelperDualPaneLayout {
        override val titleSpacerHeight = 12.dp

        override fun Modifier.content() = padding(horizontal = 24.dp)

        override fun Modifier.titleRow() = this

        @Composable override fun Modifier.sidebar() = width(240.dp)

        override fun Modifier.endPanel() = padding(top = 8.dp)

        @Composable override fun RowScope.TrailingTitleActions(onCloseClicked: () -> Unit) {}
    }

    /** A dialog with a wider sidebar and its own close button. */
    data object Dialog : ShortcutHelperDualPaneLayout {
        override val titleSpacerHeight = 0.dp

        override fun Modifier.content() = this

        override fun Modifier.titleRow() =
            padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)

        @Composable
        override fun Modifier.sidebar() =
            width(dimensionResource(R.dimen.shortcut_helper_dialog_sidebar_width))
                .fillMaxHeight()
                .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 24.dp)

        override fun Modifier.endPanel() = padding(top = 8.dp, end = 24.dp, bottom = 24.dp)

        @Composable
        override fun RowScope.TrailingTitleActions(onCloseClicked: () -> Unit) {
            Spacer(Modifier.width(24.dp))
            IconButton(onClick = onCloseClicked) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.accessibility_desc_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
