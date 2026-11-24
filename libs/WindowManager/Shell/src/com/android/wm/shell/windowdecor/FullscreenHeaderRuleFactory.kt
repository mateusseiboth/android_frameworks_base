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
package com.android.wm.shell.windowdecor

import android.content.Context
import com.android.wm.shell.R

/**
 * Builds the [FullscreenHeaderRule] from `config_showFullscreenHeaderInSplitScreen` and
 * `config_keepCustomHeaderInFullscreen`.
 */
fun buildFullscreenHeaderRule(context: Context): FullscreenHeaderRule =
    FullscreenHeaderRule(
        isSplitScreenHeaderEnabled =
            context.resources.getBoolean(R.bool.config_showFullscreenHeaderInSplitScreen),
        isCustomHeaderEnabled =
            context.resources.getBoolean(R.bool.config_keepCustomHeaderInFullscreen),
    )
