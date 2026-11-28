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

import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RemoteViews
import androidx.core.view.isInvisible
import com.android.internal.policy.SystemBarUtils
import com.android.internal.statusbar.NotificationVisibility
import com.android.systemui.CoreStartable
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.display.data.repository.FocusedDisplayRepository
import com.android.systemui.res.R
import com.android.systemui.shade.domain.interactor.ShadeInteractor
import com.android.systemui.statusbar.NotificationClickNotifier
import com.android.systemui.statusbar.notification.collection.NotificationEntry
import com.android.systemui.statusbar.notification.headsup.DesktopHeadsUpCards
import com.android.systemui.statusbar.notification.headsup.HeadsUpManager
import com.android.systemui.statusbar.notification.headsup.OnHeadsUpChangedListener
import com.android.systemui.statusbar.policy.KeyguardStateController
import javax.inject.Inject
import kotlin.math.abs

/**
 * Draws the heads-up cards of a PC (config_desktopHeadsUpCards) in a window of their own at the top
 * end of the display the user works on, above the apps, the status bar and the shade: with the
 * shade closed the scene container does not lay the notification stack out, so the stack cannot
 * draw them. Each card is the notification's own heads-up view (its app's template, with its
 * actions), on the notifications' card background.
 *
 * [HeadsUpManager] still decides when a card comes and goes (20 s, restarted once by the pointer,
 * at most four). A click opens the notification; its actions work; the X or a sideways swipe
 * puts the card away; a reply action opens the notifications popup to type in.
 */
@SysUISingleton
class DesktopHeadsUpCardsWindow
@Inject
constructor(
    @Application private val context: Context,
    @Main private val resources: Resources,
    private val headsUpManager: HeadsUpManager,
    private val focusedDisplayRepository: FocusedDisplayRepository,
    private val clickNotifier: NotificationClickNotifier,
    private val shadeInteractor: ShadeInteractor,
    private val keyguardStateController: KeyguardStateController,
) : CoreStartable, OnHeadsUpChangedListener {

    /** The window with the stack of cards, on one display. */
    private class Host(val displayId: Int, val windowManager: WindowManager, val stack: LinearLayout)

    private var host: Host? = null
    private val cards = LinkedHashMap<String, View>()

    /** When each card leaves at the latest, whatever SystemUI's heads-up state says. */
    private val deadlines = HashMap<String, Long>()
    private val restartedKeys = HashSet<String>()
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutMs by lazy { DesktopHeadsUpCards.fromResources(resources).timeoutMs.toLong() }
    private val checkCards = Runnable { removeExpiredCards() }

    override fun start() {
        if (!DesktopHeadsUpCards.fromResources(resources).isEnabled) return
        headsUpManager.addListener(this)
        Log.i(TAG, "heads-up cards on")
    }

    override fun onHeadsUpStateChanged(entry: NotificationEntry, isHeadsUp: Boolean) {
        runCatching { if (isHeadsUp) showCard(entry) else hideCard(entry.key) }
            .onFailure { Log.e(TAG, "card of ${entry.key}", it) }
    }

    override fun onHeadsUpPinnedModeChanged(inPinnedMode: Boolean) {
        if (!inPinnedMode) removeExpiredCards()
    }

    /**
     * A card leaves once its heads-up is gone or at its deadline (the timeout, once restarted by
     * the pointer): nothing keeps a card forever, even if a heads-up's end went unseen.
     */
    private fun removeExpiredCards() {
        val now = SystemClock.uptimeMillis()
        cards.keys
            .filter { key -> now >= (deadlines[key] ?: 0L) || !headsUpManager.isHeadsUpEntry(key) }
            .forEach { key ->
                Log.i(TAG, "card $key expired")
                hideFromUser(key, "expired")
            }
        handler.removeCallbacks(checkCards)
        if (cards.isNotEmpty()) handler.postDelayed(checkCards, CHECK_INTERVAL_MS)
    }

    private fun showCard(entry: NotificationEntry) {
        hideCardNow(entry.key)
        // Over the lock screen the shade's own heads-up shows, with its privacy rules.
        if (keyguardStateController.isShowing) return
        val host = obtainHost()
        val card = buildCard(host.stack.context, entry)
        cards[entry.key] = card
        deadlines[entry.key] = SystemClock.uptimeMillis() + timeoutMs + ANIMATION_MS
        restartedKeys.remove(entry.key)
        handler.removeCallbacks(checkCards)
        handler.postDelayed(checkCards, CHECK_INTERVAL_MS)
        host.stack.addView(card, 0, buildCardLayoutParams(host.stack.context))
        val slide = resources.displayMetrics.density * SLIDE_DP
        card.alpha = 0f
        card.translationX = slide
        card.animate().alpha(1f).translationX(0f).setDuration(ANIMATION_MS).start()
        Log.i(TAG, "card ${entry.key} on display ${host.displayId}, ${cards.size} shown")
    }

    private fun hideCard(key: String) {
        deadlines.remove(key)
        val card = cards.remove(key) ?: return
        val slide = resources.displayMetrics.density * SLIDE_DP
        card.animate().alpha(0f).translationX(slide).setDuration(ANIMATION_MS).withEndAction {
            (card.parent as? ViewGroup)?.removeView(card)
            removeHostIfEmpty()
        }.start()
        Log.i(TAG, "card $key gone, ${cards.size} shown")
    }

    private fun hideCardNow(key: String) {
        deadlines.remove(key)
        val card = cards.remove(key) ?: return
        (card.parent as? ViewGroup)?.removeView(card)
    }

    /** The window on the focused display; it moves there when it holds no card. */
    private fun obtainHost(): Host {
        val displayId = findDisplayId()
        host?.takeIf { it.displayId == displayId || cards.isNotEmpty() }?.let { return it }
        host?.let(::removeHost)
        return createHost(displayId).also { host = it }
    }

    private fun findDisplayId(): Int {
        val displays = context.getSystemService(DisplayManager::class.java)
        val focused = focusedDisplayRepository.focusedDisplayId.value
        return focused.takeIf { displays?.getDisplay(it) != null } ?: Display.DEFAULT_DISPLAY
    }

    private fun createHost(displayId: Int): Host {
        val display = requireNotNull(context.getSystemService(DisplayManager::class.java))
            .getDisplay(displayId)
        val windowContext =
            context.createDisplayContext(display)
                .createWindowContext(WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL, null)
        val themed = android.view.ContextThemeWrapper(windowContext, R.style.Theme_SystemUI)
        val stack = LinearLayout(themed).apply { orientation = LinearLayout.VERTICAL }
        val windowManager = requireNotNull(windowContext.getSystemService(WindowManager::class.java))
        windowManager.addView(stack, buildWindowLayoutParams(themed))
        return Host(displayId, windowManager, stack)
    }

    private fun removeHostIfEmpty() {
        if (cards.isEmpty()) host?.let(::removeHost)
    }

    private fun removeHost(old: Host) {
        runCatching { old.windowManager.removeViewImmediate(old.stack) }
        if (host === old) host = null
    }

    private fun buildWindowLayoutParams(context: Context): WindowManager.LayoutParams {
        val res = context.resources
        return WindowManager.LayoutParams(
                res.getDimensionPixelSize(R.dimen.shade_panel_width),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT,
            )
            .apply {
                gravity = Gravity.TOP or Gravity.END
                x = findEdgeMargin(res)
                y = SystemBarUtils.getStatusBarHeight(context) +
                    res.getDimensionPixelSize(R.dimen.desktop_heads_up_card_margin_top)
                title = TAG
                privateFlags =
                    privateFlags or WindowManager.LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
                setFitInsetsTypes(0)
            }
    }

    private fun findEdgeMargin(res: Resources): Int =
        res.getDimensionPixelSize(R.dimen.shade_panel_margin_horizontal)
            .coerceAtLeast(res.getDimensionPixelSize(R.dimen.desktop_heads_up_card_margin_top))

    private fun buildCardLayoutParams(context: Context): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            .apply {
                bottomMargin = context.resources.getDimensionPixelSize(R.dimen.desktop_heads_up_card_gap)
            }

    private fun buildCard(themed: Context, entry: NotificationEntry): View {
        lateinit var close: View
        val card =
            CardView(
                themed,
                onSwipedAway = { hideFromUser(entry.key, "swiped") },
                onHover = { event -> onCardHover(entry, close, event) },
            )
        card.background =
            GradientDrawable().apply {
                setColor(themed.getColor(com.android.internal.R.color.materialColorSurfaceContainerHigh))
                cornerRadius = themed.resources.getDimension(R.dimen.notification_corner_radius)
            }
        card.clipToOutline = true
        card.elevation = themed.resources.displayMetrics.density * ELEVATION_DP
        card.addView(buildContent(themed, entry, card))
        close = buildCloseButton(themed, entry)
        card.addView(close)
        card.setOnClickListener { onCardClicked(entry) }
        return card
    }

    /** The app's heads-up template (or its content view), with its actions working. */
    private fun buildContent(themed: Context, entry: NotificationEntry, parent: ViewGroup): View {
        val packageContext = entry.sbn.getPackageContext(themed)
        val builder = Notification.Builder.recoverBuilder(packageContext, entry.sbn.notification)
        val views = builder.createHeadsUpContentView() ?: builder.createContentView()
        return views.apply(packageContext, parent) { view, pendingIntent, response ->
            onAction(view, pendingIntent, response)
        }
    }

    private fun onAction(
        view: View,
        pendingIntent: PendingIntent,
        response: RemoteViews.RemoteResponse,
    ): Boolean {
        // A reply needs the notification's own input field: the notifications popup has it.
        if (view.getTag(com.android.internal.R.id.remote_input_tag) is Array<*>) {
            shadeInteractor.expandNotificationsShade("desktop heads-up card reply")
            return true
        }
        return RemoteViews.startPendingIntent(view, pendingIntent, response.getLaunchOptions(view))
    }

    private fun onCardClicked(entry: NotificationEntry) {
        val contentIntent = entry.sbn.notification.contentIntent
        if (contentIntent == null) {
            shadeInteractor.expandNotificationsShade("desktop heads-up card without intent")
            hideFromUser(entry.key, "clicked")
            return
        }
        val options =
            ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS
                )
        runCatching { contentIntent.send(context, 0, null, null, null, null, options.toBundle()) }
            .onFailure { Log.e(TAG, "open ${entry.key}", it) }
        // The system cancels an auto cancel notification on this click, as for the shade's rows.
        clickNotifier.onNotificationClick(
            entry.key,
            NotificationVisibility.obtain(entry.key, entry.ranking.rank, cards.size, true),
        )
        hideFromUser(entry.key, "clicked")
    }

    /** The pointer restarts the card's time once ([HeadsUpManager]) and shows the X. */
    private fun onCardHover(entry: NotificationEntry, close: View, event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_HOVER_ENTER) {
            headsUpManager.setHeadsUpDismissTimerPaused(entry.key, true)
            // The pointer gives the card its whole time again, only once.
            if (restartedKeys.add(entry.key) && entry.key in deadlines) {
                deadlines[entry.key] = SystemClock.uptimeMillis() + timeoutMs
            }
        }
        close.isInvisible = event.actionMasked == MotionEvent.ACTION_HOVER_EXIT
    }

    private fun buildCloseButton(themed: Context, entry: NotificationEntry): View {
        val size = (themed.resources.displayMetrics.density * CLOSE_SIZE_DP).toInt()
        return ImageButton(themed).apply {
            // The same X as the notification rows' on hover.
            setImageResource(R.drawable.notification_dismiss_button_icon)
            setBackgroundResource(R.drawable.dismiss_button_bg)
            contentDescription = themed.getString(R.string.notification_dismiss_button_text)
            visibility = View.INVISIBLE
            layoutParams =
                FrameLayout.LayoutParams(size, size, Gravity.TOP or Gravity.END).apply {
                    val margin = size / 4
                    setMargins(margin, margin, margin, margin)
                }
            setOnClickListener { hideFromUser(entry.key, "closed") }
        }
    }

    /** The card goes away; the notification stays in the list. */
    private fun hideFromUser(key: String, reason: String) {
        headsUpManager.removeNotification(key, /* releaseImmediately= */ true, "desktop card $reason")
        hideCard(key)
    }

    /**
     * A card that a sideways swipe (finger or mouse drag) puts away. It sees every hover event over
     * it, also those over its children (the X), so the X does not flicker.
     */
    private class CardView(
        context: Context,
        private val onSwipedAway: () -> Unit,
        private val onHover: (MotionEvent) -> Unit,
    ) : FrameLayout(context) {

        override fun dispatchHoverEvent(event: MotionEvent): Boolean {
            onHover(event)
            return super.dispatchHoverEvent(event)
        }

        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        private var downX = 0f
        private var isSwiping = false

        override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                downX = event.rawX
                isSwiping = false
            }
            if (event.actionMasked == MotionEvent.ACTION_MOVE && abs(event.rawX - downX) > touchSlop) {
                isSwiping = true
            }
            return isSwiping
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (!isSwiping) return super.onTouchEvent(event)
            val dx = event.rawX - downX
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    translationX = dx
                    alpha = 1f - (abs(dx) / width).coerceIn(0f, 1f)
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> finishSwipe(dx)
            }
            return true
        }

        private fun finishSwipe(dx: Float) {
            isSwiping = false
            if (abs(dx) > width / SWIPE_AWAY_FRACTION) {
                onSwipedAway()
                return
            }
            animate().translationX(0f).alpha(1f).setDuration(ANIMATION_MS).start()
        }
    }

    private companion object {
        const val TAG = "DesktopHeadsUpCards"
        const val ANIMATION_MS = 250L
        const val SLIDE_DP = 48f
        const val ELEVATION_DP = 8f
        const val CLOSE_SIZE_DP = 32f
        const val SWIPE_AWAY_FRACTION = 3
        const val CHECK_INTERVAL_MS = 1000L
    }
}
