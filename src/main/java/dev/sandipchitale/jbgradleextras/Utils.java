package dev.sandipchitale.jbgradleextras;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class Utils {
    private static final NotificationGroup NOTIFICATIONS_GROUP = NotificationGroupManager.getInstance()
            .getNotificationGroup("dev.sandipchitale.jbgradleextras.notifications");

    static void showNotification(Project project, String title, String content) {
        NOTIFICATIONS_GROUP.createNotification(
                        title,
                        content,
                        NotificationType.INFORMATION)
                .notify(project);
    }

}
