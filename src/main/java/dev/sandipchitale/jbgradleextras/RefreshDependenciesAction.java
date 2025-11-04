package dev.sandipchitale.jbgradleextras;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static dev.sandipchitale.jbgradleextras.Constants.GRADLE;

public class RefreshDependenciesAction extends AnAction {

    public static final NotificationGroup NOTIFICATIONS_GROUP = NotificationGroupManager.getInstance()
            .getNotificationGroup("dev.sandipchitale.jbgradleextras.notifications");

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

            NOTIFICATIONS_GROUP.createNotification(
                    "Dependencies",
                    "Refreshing dependencies",
                    NotificationType.INFORMATION)
                    .notify(project);

            // 1. Configure the task execution
            ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();
            settings.setExternalSystemIdString(GRADLE.getId());
            settings.setExternalProjectPath(Objects.requireNonNull(project).getBasePath());
            settings.setScriptParameters("--refresh-dependencies -x help");

            // 2. Run the task
            ExternalSystemUtil.runTask(
                    settings,
                    DefaultRunExecutor.EXECUTOR_ID, // Use the standard "Run" executor
                    project,
                    GRADLE,
                    null,
                    ProgressExecutionMode.IN_BACKGROUND_ASYNC,
                    true,
                    null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        Presentation presentation = anActionEvent.getPresentation();
        presentation.setEnabledAndVisible(true);
    }
}
