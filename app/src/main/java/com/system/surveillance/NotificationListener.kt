package com.system.surveillance

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Placeholder for handling notifications
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Placeholder for handling notification removal
    }
}
