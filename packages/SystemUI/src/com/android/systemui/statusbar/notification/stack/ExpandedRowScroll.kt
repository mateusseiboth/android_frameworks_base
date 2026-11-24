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

@file:JvmName("ExpandedRowScroll")

package com.android.systemui.statusbar.notification.stack

import kotlin.math.min

/**
 * How far (px) the notification stack scrolls so a row the user just expanded is in view: by as
 * much as the row's bottom passes the bottom of the visible stack, but never so far that the row's
 * top leaves the top of the visible stack. 0 when the row needs no scrolling.
 */
fun scrollToRevealExpandedRow(
    rowTop: Float,
    rowBottom: Float,
    visibleTop: Float,
    visibleBottom: Float,
): Float {
    val hiddenBelow = rowBottom - visibleBottom
    if (hiddenBelow <= 0f) return 0f
    val roomAbove = rowTop - visibleTop
    if (roomAbove <= 0f) return 0f
    return min(hiddenBelow, roomAbove)
}
