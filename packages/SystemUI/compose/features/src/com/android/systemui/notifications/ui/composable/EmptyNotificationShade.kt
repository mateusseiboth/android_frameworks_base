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

package com.android.systemui.notifications.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.systemui.common.ui.compose.Icon
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.statusbar.notification.emptyshade.ui.shared.model.IconMessageModel
import com.android.systemui.statusbar.notification.emptyshade.ui.viewmodel.EmptyShadeViewModel

/**
 * The empty state of the notifications shade overlay: an icon over the message of the empty shade
 * ("You're all caught up", or the mode that hides notifications), centered in the panel.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyNotificationShade(
    viewModelFactory: EmptyShadeViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember(viewModelFactory) { viewModelFactory.create() }
    val message: IconMessageModel? by
        viewModel.message.collectAsStateWithLifecycle(initialValue = null)
    val shown = message ?: return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EmptyNotificationShadeDimensions.IconSpacing),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = EmptyNotificationShadeDimensions.PaddingHorizontal,
                    vertical = EmptyNotificationShadeDimensions.PaddingVertical,
                )
                .sysuiResTag("empty_shade_content"),
    ) {
        Icon(
            icon = shown.icon,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(EmptyNotificationShadeDimensions.IconSize),
        )
        Text(
            text = shown.message,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMediumEmphasized,
            textAlign = TextAlign.Center,
        )
    }
}

private object EmptyNotificationShadeDimensions {
    val PaddingHorizontal = 16.dp
    val PaddingVertical = 32.dp
    val IconSize = 32.dp
    val IconSpacing = 16.dp
}
