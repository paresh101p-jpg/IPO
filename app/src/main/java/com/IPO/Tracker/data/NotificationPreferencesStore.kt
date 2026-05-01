package com.IPO.Tracker.data

import android.content.Context

object NotificationPreferencesStore {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_LISTING_ALERT = "listing_alert"
    private const val KEY_ALLOTMENT_ALERT = "allotment_alert"
    private const val KEY_GMP_ALERT = "gmp_alert"
    private const val KEY_NEWS_ALERT = "news_alert"
    private const val KEY_IPO_NOTIFICATION_IDS = "ipo_notification_ids"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isListingAlertEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_LISTING_ALERT, true)
    fun setListingAlertEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_LISTING_ALERT, enabled).apply()
    }

    fun isAllotmentAlertEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ALLOTMENT_ALERT, true)
    fun setAllotmentAlertEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ALLOTMENT_ALERT, enabled).apply()
    }

    fun isGmpAlertEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_GMP_ALERT, true)
    fun setGmpAlertEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_GMP_ALERT, enabled).apply()
    }

    fun isNewsAlertEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_NEWS_ALERT, true)
    fun setNewsAlertEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NEWS_ALERT, enabled).apply()
    }

    fun getIpoNotificationIds(context: Context): Set<String> = prefs(context).getStringSet(KEY_IPO_NOTIFICATION_IDS, emptySet()) ?: emptySet()

    fun isIpoNotificationEnabled(context: Context, ipoId: String): Boolean {
        return getIpoNotificationIds(context).contains(ipoId)
    }

    fun toggleIpoNotification(context: Context, ipoId: String): Boolean {
        val ids = getIpoNotificationIds(context).toMutableSet()
        val result = if (ids.contains(ipoId)) {
            ids.remove(ipoId)
            false
        } else {
            ids.add(ipoId)
            true
        }
        prefs(context).edit().putStringSet(KEY_IPO_NOTIFICATION_IDS, ids).apply()
        return result
    }
}
