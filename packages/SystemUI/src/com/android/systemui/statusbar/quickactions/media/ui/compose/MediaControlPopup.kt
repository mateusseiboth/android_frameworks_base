/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.systemui.statusbar.quickactions.media.ui.compose

import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.ui.graphics.painter.rememberDrawablePainter
import com.android.systemui.media.remedia.shared.flag.MediaControlsInComposeFlag
import com.android.systemui.media.remedia.ui.compose.Media
import com.android.systemui.media.remedia.ui.compose.MediaPresentationStyle
import com.android.systemui.media.remedia.ui.compose.MediaUiBehavior
import com.android.systemui.media.remedia.ui.viewmodel.MediaCarouselVisibility
import com.android.systemui.res.R
import com.android.systemui.statusbar.quickactions.media.data.ActiveMediaSessions
import com.android.systemui.statusbar.quickactions.media.ui.viewmodel.MediaControlPopupViewModel

/** Displays a popup containing media controls. Embeds the MediaCarousel within a Compose popup. */
@Composable
fun MediaControlPopup(viewModel: MediaControlPopupViewModel, modifier: Modifier = Modifier) {
    val session by viewModel.activeSession.collectAsStateWithLifecycle()
    val playing = session
    if (MediaControlsInComposeFlag.isEnabled && !viewModel.hasMediaCards && playing != null) {
        SessionControls(session = playing, modifier = modifier.width(400.dp))
    } else if (MediaControlsInComposeFlag.isEnabled) {
        Media(
            viewModelFactory = viewModel.mediaViewModelFactory,
            presentationStyle = MediaPresentationStyle.Default,
            behavior =
                MediaUiBehavior(
                    carouselVisibility = MediaCarouselVisibility.WhenAnyCardIsActive,
                    isCarouselDismissible = false,
                    isCarouselScrollingEnabled = false,
                ),
            onDismissed = {},
            modifier =
                modifier
                    .width(400.dp)
                    .height(200.dp)
                    .clip(
                        shape =
                            RoundedCornerShape(
                                dimensionResource(R.dimen.notification_corner_radius)
                            )
                    ),
            location = Media.Location.STATUS_BAR_POPUP,
        )
    } else {
        AndroidView(
            modifier =
                modifier
                    .width(400.dp)
                    .height(200.dp)
                    .clip(
                        shape =
                            RoundedCornerShape(
                                dimensionResource(R.dimen.notification_corner_radius)
                            )
                    ),
            factory = { _ ->
                viewModel.mediaHost.hostView.apply {
                    layoutParams =
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                        )
                }
                viewModel.mediaHost.hostView
            },
            onReset = {},
        )
    }
}

/**
 * The controls of what plays without a media card (a video in the foreground posts no media
 * notification): the app, the title and artist, previous, play or pause, next.
 */
@Composable
private fun SessionControls(session: ActiveMediaSessions.Session, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val controls = session.controller.transportControls
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val icon = remember(session.appIcon) { session.appIcon?.loadDrawable(context) }
        icon?.let {
            Image(
                painter = rememberDrawablePainter(it),
                contentDescription = session.appName,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = (session.artist ?: session.appName).toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SessionButton(
            iconRes = R.drawable.ic_media_prev,
            description = stringResource(R.string.controls_media_button_prev),
            onClick = { controls.skipToPrevious() },
        )
        SessionButton(
            iconRes =
                android.R.drawable.ic_media_pause.takeIf { session.isPlaying }
                    ?: android.R.drawable.ic_media_play,
            description =
                stringResource(
                    R.string.controls_media_button_pause.takeIf { session.isPlaying }
                        ?: R.string.controls_media_button_play
                ),
            onClick = { if (session.isPlaying) controls.pause() else controls.play() },
        )
        SessionButton(
            iconRes = R.drawable.ic_media_next,
            description = stringResource(R.string.controls_media_button_next),
            onClick = { controls.skipToNext() },
        )
    }
}

@Composable
private fun SessionButton(iconRes: Int, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
    }
}
