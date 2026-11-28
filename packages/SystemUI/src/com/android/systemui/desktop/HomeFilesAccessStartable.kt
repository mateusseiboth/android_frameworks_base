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

package com.android.systemui.desktop

import android.Manifest
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.UserHandle
import android.util.Log
import com.android.systemui.CoreStartable
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.settings.UserTracker
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * Lets the home app show the files of the Desktop folder on a PC. Its all files access
 * (MANAGE_EXTERNAL_STORAGE) is granted at install, but the app op behind it starts at its default,
 * which denies, and the launcher is not platform signed to switch it on itself. SystemUI does it once
 * per user, for system home apps that ask for it and whose op nobody has set, then restarts the home
 * app so it picks the access up (the framework only restarts an app when the access is taken away).
 */
@SysUISingleton
class HomeFilesAccessStartable
@Inject
constructor(
    @Application private val context: Context,
    @Background private val bgExecutor: Executor,
    private val userTracker: UserTracker,
) : CoreStartable {

    override fun start() {
        userTracker.addCallback(
            object : UserTracker.Callback {
                override fun onUserChanged(newUser: Int, userContext: Context) = allowFor(newUser)
            },
            bgExecutor,
        )
        bgExecutor.execute { allowFor(userTracker.userId) }
    }

    private fun allowFor(userId: Int) {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        context.packageManager
            .queryIntentActivitiesAsUser(home, MATCH_ANY_BOOT_STATE or PackageManager.MATCH_SYSTEM_ONLY, userId)
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { isAskingForFiles(it, userId) }
            .forEach { allowPackage(it, userId) }
    }

    private fun isAskingForFiles(packageName: String, userId: Int): Boolean =
        try {
            val info =
                context.packageManager.getPackageInfoAsUser(
                    packageName,
                    MATCH_ANY_BOOT_STATE or PackageManager.GET_PERMISSIONS,
                    userId,
                )
            val isSystem = (info.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
            isSystem && info.requestedPermissions?.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE) == true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

    private fun allowPackage(packageName: String, userId: Int) {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return
        val uid =
            try {
                context.packageManager.getPackageUidAsUser(packageName, userId)
            } catch (_: PackageManager.NameNotFoundException) {
                return
            }
        val op = AppOpsManager.OPSTR_MANAGE_EXTERNAL_STORAGE
        // A choice the user made in Settings stays: only the untouched default is switched on.
        if (appOps.unsafeCheckOpRawNoThrow(op, uid, packageName) != AppOpsManager.MODE_DEFAULT) return
        try {
            appOps.setMode(op, uid, packageName, AppOpsManager.MODE_ALLOWED)
            context
                .getSystemService(ActivityManager::class.java)
                ?.forceStopPackageAsUser(packageName, userId)
            Log.i(TAG, "All files access on for $packageName (user $userId)")
        } catch (error: SecurityException) {
            Log.w(TAG, "Cannot switch all files access on for $packageName", error)
        }
    }

    private companion object {
        const val TAG = "HomeFilesAccess"

        // The user is switched to before it is unlocked: a home app that is not direct boot aware only
        // resolves with both flags.
        const val MATCH_ANY_BOOT_STATE =
            PackageManager.MATCH_DIRECT_BOOT_AWARE or PackageManager.MATCH_DIRECT_BOOT_UNAWARE
    }
}
