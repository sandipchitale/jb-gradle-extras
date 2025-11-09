package dev.sandipchitale.jbgradleextras;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.settings.GradleSettings;

import java.util.Objects;

import static dev.sandipchitale.jbgradleextras.Constants.GRADLE;

public class RefreshDependenciesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        Utils.showNotification(project,
                "Dependencies",
                "Refreshing dependencies");

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
        Project project = anActionEvent.getProject();
        presentation.setEnabledAndVisible(!GradleSettings.getInstance(Objects.requireNonNull(project)).getLinkedProjectsSettings().isEmpty());
    }
}
