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
package com.android.wm.shell.windowdecor.common

/**
 * Whether a window header shows the label and icon an app sets on its task description (and so
 * changes them live), instead of the ones of its launch activity.
 *
 * @param isEnabled whether headers follow task descriptions at all.
 * @param packageAllowlist the apps whose headers follow them; empty means every app.
 */
class TaskDescriptionHeaderRule(
    private val isEnabled: Boolean,
    private val packageAllowlist: Set<String>,
) {

    /**
     * Whether the header of a task follows its task description. It does not when an activity of
     * another app is on top of the task, since the description then describes that other app.
     */
    fun isFollowing(basePackage: String?, topPackage: String?): Boolean {
        if (!isEnabled || basePackage == null) return false
        if (topPackage != null && topPackage != basePackage) return false
        return packageAllowlist.isEmpty() || basePackage in packageAllowlist
    }

    /** The header title: the task description's label when it has one, else [fallback]. */
    fun pickLabel(taskDescriptionLabel: CharSequence?, fallback: CharSequence): CharSequence =
        taskDescriptionLabel?.takeIf { it.isNotBlank() } ?: fallback
}
