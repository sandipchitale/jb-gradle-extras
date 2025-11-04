package dev.sandipchitale.jbgradleextras;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemProgressNotificationManager;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.externalSystem.view.ExternalSystemNode;
import com.intellij.openapi.externalSystem.view.TaskNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static dev.sandipchitale.jbgradleextras.Constants.GRADLE;

public class GradleTaskHelpAction extends AnAction {


    static class OutputDialog extends DialogWrapper {
        private final String output;

        public OutputDialog(Project project, String output, String taskName) {
            super(project);
            this.output = output;
            setTitle("Task: " + taskName);

            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JTextArea textArea = new JTextArea(output);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JBScrollPane scrollPane = new JBScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(800, 600));
            return scrollPane;
        }

        @Override
        protected Action @NotNull [] createActions() {
            // Only show OK button
            Action okAction = getOKAction();
            return new Action[]{okAction};
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        List<ExternalSystemNode> selectedNodes = getSelectedTaskData(anActionEvent);
        if (selectedNodes != null && !selectedNodes.isEmpty() && (selectedNodes.getFirst() instanceof TaskNode taskNode)) {
            // Get the simple task name (e.g., "build")
            String taskName = taskNode.getName();

            Utils.showNotification(project,
                    "Task: " + taskName,
                    "Getting detailed information about task: " + taskName);

            ExternalSystemProgressNotificationManager notificationManager =
                    ExternalSystemProgressNotificationManager.getInstance();

            ExternalSystemTaskNotificationListener listener =
                    new ExternalSystemTaskNotificationListener() {
                        final StringBuilder output = new StringBuilder();

                        @Override
                        public void onTaskOutput(@NotNull ExternalSystemTaskId id, @NotNull String text, @NotNull ProcessOutputType outputType) {
                            output.append(text);
                        }

                        @Override
                        public void onEnd(@NotNull String projectPath, @NotNull ExternalSystemTaskId id) {
                            notificationManager.removeNotificationListener(this);
                            ApplicationManager.getApplication().invokeLater(() -> new OutputDialog(project, output.toString(), taskName).show());
                        }
                    };

            notificationManager.addNotificationListener(listener);

            // 1. Configure the task execution
            ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();
            settings.setExternalSystemIdString(GRADLE.getId());
            settings.setExternalProjectPath(Objects.requireNonNull(project).getBasePath());
            settings.setTaskNames(Collections.singletonList("help"));
            settings.setScriptParameters("-q --task " + taskName);

            // 2. Run the task
            ExternalSystemUtil.runTask(
                    settings,
                    DefaultRunExecutor.EXECUTOR_ID,
                    project,
                    GRADLE,
                    null,
                    ProgressExecutionMode.IN_BACKGROUND_ASYNC,
                    false,
                    null);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        List<ExternalSystemNode> selectedNodes = getSelectedTaskData(anActionEvent);
        Presentation presentation = anActionEvent.getPresentation();
        boolean taskNodeSelected = selectedNodes != null && !selectedNodes.isEmpty() && (selectedNodes.getFirst() instanceof TaskNode);
        if (taskNodeSelected) {
            presentation.setText("Detailed Task Information for: " + selectedNodes.getFirst().getName());
        } else {
            presentation.setText("Show Detailed Task Information");
        }
        presentation.setEnabled(taskNodeSelected);
    }

    /**
     * Helper to get the TaskData if a single task is selected.
     */
    private List<ExternalSystemNode> getSelectedTaskData(@NotNull AnActionEvent anActionEvent) {
        return anActionEvent.getData(ExternalSystemDataKeys.SELECTED_NODES);
    }
}
