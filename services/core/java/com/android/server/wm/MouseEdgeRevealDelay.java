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

package com.android.server.wm;

/**
 * How long a mouse pointer rests at a display edge before the hidden system bars there are
 * revealed. The top and bottom edges (status bar and window header, taskbar) take their delay from
 * {@code config_mouseEdgeRevealDelayMs}; the side edges keep {@link #DEFAULT_DELAY_MS}.
 */
final class MouseEdgeRevealDelay {

    /** The delay AOSP uses. */
    static final long DEFAULT_DELAY_MS = 500;

    private MouseEdgeRevealDelay() {}

    /**
     * The delay from {@code config_mouseEdgeRevealDelayMs}; a negative value is a broken overlay
     * and keeps the default.
     */
    static long fromConfig(int configuredDelayMs) {
        if (configuredDelayMs < 0) return DEFAULT_DELAY_MS;
        return configuredDelayMs;
    }
}
