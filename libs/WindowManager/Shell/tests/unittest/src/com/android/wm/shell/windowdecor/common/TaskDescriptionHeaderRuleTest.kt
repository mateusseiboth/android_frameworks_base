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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [TaskDescriptionHeaderRule]. */
class TaskDescriptionHeaderRuleTest {

    private val everyApp = TaskDescriptionHeaderRule(isEnabled = true, packageAllowlist = emptySet())

    @Test
    fun isFollowing_disabled_isFalse() {
        val rule = TaskDescriptionHeaderRule(isEnabled = false, packageAllowlist = emptySet())

        assertFalse(rule.isFollowing(basePackage = BROWSER, topPackage = BROWSER))
    }

    @Test
    fun isFollowing_enabledWithoutAllowlist_followsEveryApp() {
        assertTrue(everyApp.isFollowing(basePackage = BROWSER, topPackage = BROWSER))
    }

    @Test
    fun isFollowing_allowlistedApp_isTrue() {
        val rule = TaskDescriptionHeaderRule(isEnabled = true, packageAllowlist = setOf(BROWSER))

        assertTrue(rule.isFollowing(basePackage = BROWSER, topPackage = BROWSER))
    }

    @Test
    fun isFollowing_appOutsideTheAllowlist_isFalse() {
        val rule = TaskDescriptionHeaderRule(isEnabled = true, packageAllowlist = setOf(BROWSER))

        assertFalse(rule.isFollowing(basePackage = MAIL, topPackage = MAIL))
    }

    @Test
    fun isFollowing_anotherAppOnTopOfTheTask_isFalse() {
        assertFalse(everyApp.isFollowing(basePackage = BROWSER, topPackage = MAIL))
    }

    @Test
    fun isFollowing_unknownTopActivity_followsTheBaseApp() {
        assertTrue(everyApp.isFollowing(basePackage = BROWSER, topPackage = null))
    }

    @Test
    fun isFollowing_unknownBaseApp_isFalse() {
        assertFalse(everyApp.isFollowing(basePackage = null, topPackage = BROWSER))
    }

    @Test
    fun pickLabel_taskDescriptionLabel_winsOverTheActivityLabel() {
        assertEquals("Inbox (3)", everyApp.pickLabel("Inbox (3)", fallback = "Mail"))
    }

    @Test
    fun pickLabel_withoutTaskDescriptionLabel_usesTheActivityLabel() {
        assertEquals("Mail", everyApp.pickLabel(null, fallback = "Mail"))
    }

    @Test
    fun pickLabel_blankTaskDescriptionLabel_usesTheActivityLabel() {
        assertEquals("Mail", everyApp.pickLabel("  ", fallback = "Mail"))
    }

    private companion object {
        const val BROWSER = "org.example.browser"
        const val MAIL = "org.example.mail"
    }
}
