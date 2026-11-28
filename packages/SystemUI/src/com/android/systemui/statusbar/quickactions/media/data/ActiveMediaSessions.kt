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

package com.android.systemui.statusbar.quickactions.media.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.UserHandle
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.settings.UserTracker
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What the user on screen plays, straight from the media sessions: apps that play in the foreground
 * (a video in YouTube's window) post no media notification, so the media pipeline, which follows
 * notifications, never sees them. SystemUI holds MEDIA_CONTENT_CONTROL, and under HSUM listens to
 * the current user's sessions, not its own (user 0).
 */
@SysUISingleton
class ActiveMediaSessions
@Inject
constructor(
    @Application private val context: Context,
    @Main private val mainHandler: Handler,
    @Main private val mainExecutor: Executor,
    private val userTracker: UserTracker,
) {
    /** The media a session plays, and its controls. */
    data class Session(
        val controller: MediaController,
        val appName: String,
        val appIcon: Icon?,
        val title: CharSequence,
        val artist: CharSequence?,
        val isPlaying: Boolean,
    )

    private val sessionManager = context.getSystemService(MediaSessionManager::class.java)
    private val mutableCurrent = MutableStateFlow<Session?>(null)
    private var controllers: List<MediaController> = emptyList()
    private var isStarted = false

    /** The playing session (else a paused one with a title), or null. */
    val current: StateFlow<Session?> = mutableCurrent.asStateFlow()

    private val controllerCallback =
        object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) = update()

            override fun onMetadataChanged(metadata: MediaMetadata?) = update()

            override fun onSessionDestroyed() = update()
        }

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { list -> setControllers(list.orEmpty()) }

    fun start() {
        if (isStarted || sessionManager == null) return
        isStarted = true
        userTracker.addCallback(
            object : UserTracker.Callback {
                override fun onUserChanged(newUser: Int, userContext: Context) =
                    listenTo(UserHandle.of(newUser))
            },
            mainExecutor,
        )
        listenTo(userTracker.userHandle)
    }

    private fun listenTo(user: UserHandle) {
        val manager = sessionManager ?: return
        runCatching {
                manager.removeOnActiveSessionsChangedListener(sessionsListener)
                manager.addOnActiveSessionsChangedListener(null, user, mainExecutor, sessionsListener)
                setControllers(manager.getActiveSessionsForUser(null, user))
            }
            .onFailure { Log.e(TAG, "sessions of user ${user.identifier}", it) }
        Log.i(TAG, "sessions of user ${user.identifier}: ${controllers.size}")
    }

    private fun setControllers(list: List<MediaController>) {
        controllers.forEach { it.unregisterCallback(controllerCallback) }
        controllers = list
        list.forEach { it.registerCallback(controllerCallback, mainHandler) }
        update()
    }

    private fun update() {
        val session =
            controllers
                .mapNotNull(::buildSession)
                .sortedByDescending { it.isPlaying }
                .firstOrNull()
        if (session?.title != mutableCurrent.value?.title || session?.isPlaying != mutableCurrent.value?.isPlaying) {
            Log.i(TAG, "session: ${session?.appName} \"${session?.title}\" playing=${session?.isPlaying}")
        }
        mutableCurrent.value = session
    }

    private fun buildSession(controller: MediaController): Session? {
        val state = controller.playbackState?.state ?: return null
        if (state !in SHOWN_STATES) return null
        val metadata = controller.metadata
        val title =
            metadata?.getText(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
                ?: metadata?.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)?.takeIf {
                    it.isNotBlank()
                }
                ?: return null
        val (appName, appIcon) = readApp(controller)
        return Session(
            controller = controller,
            appName = appName,
            appIcon = appIcon,
            title = title,
            artist = metadata?.getText(MediaMetadata.METADATA_KEY_ARTIST),
            isPlaying = state in PLAYING_STATES,
        )
    }

    /** The app's label and icon, for the user who owns the session. */
    private fun readApp(controller: MediaController): Pair<String, Icon?> {
        val packageName = controller.packageName
        val user = controller.sessionToken.uid.let(UserHandle::getUserHandleForUid)
        return runCatching {
                val packages = context.packageManager
                val info =
                    packages.getApplicationInfoAsUser(
                        packageName,
                        PackageManager.ApplicationInfoFlags.of(0),
                        user,
                    )
                val icon = packages.getApplicationIcon(info).toBitmap()
                packages.getApplicationLabel(info).toString() to Icon.createWithBitmap(icon)
            }
            .getOrElse { packageName to null }
    }

    private companion object {
        const val TAG = "MediaControlChip"
        val PLAYING_STATES =
            setOf(PlaybackState.STATE_PLAYING, PlaybackState.STATE_BUFFERING, PlaybackState.STATE_FAST_FORWARDING, PlaybackState.STATE_REWINDING)
        val SHOWN_STATES = PLAYING_STATES + PlaybackState.STATE_PAUSED
    }
}
