package cz.upce.fei.nnptp.taskpulse.cli;

import cz.upce.fei.nnptp.taskpulse.service.ProjectService;
import cz.upce.fei.nnptp.taskpulse.service.TaskService;
import cz.upce.fei.nnptp.taskpulse.storage.InMemoryProjectRepository;
import cz.upce.fei.nnptp.taskpulse.storage.InMemoryTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class TaskPulseCliTest {

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private ProjectService projectService;
    private TaskService taskService;
    private TaskPulseCli cli;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outContent, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errContent, true, StandardCharsets.UTF_8);

        InMemoryProjectRepository projectRepo = new InMemoryProjectRepository();
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        projectService = new ProjectService(projectRepo, taskRepo);
        taskService = new TaskService(taskRepo, projectRepo);

        cli = new TaskPulseCli(projectService, taskService, out, err);
    }

    @Test
    void helpCommandDisplaysAvailableCommands() {
        boolean continueRunning = cli.executeLine("help");
        assertThat(continueRunning).isTrue();
        assertThat(outContent.toString()).contains("Available commands:", "project add", "task add", "worklog log");
    }

    @Test
    void exitCommandReturnsFalse() {
        boolean continueRunning = cli.executeLine("exit");
        assertThat(continueRunning).isFalse();

        continueRunning = cli.executeLine("quit");
        assertThat(continueRunning).isFalse();
    }

    @Test
    void projectLifecycleViaCli() {
        cli.executeLine("project add \"Programming Course\" \"Semester project\"");
        assertThat(outContent.toString()).contains("Project created:", "Programming Course");

        outContent.reset();
        cli.executeLine("project list");
        assertThat(outContent.toString()).contains("Programming Course", "Semester project");
    }

    @Test
    void taskAndWorklogLifecycleViaCli() {
        var project = projectService.createProject("Course", "Desc");
        String projectId = project.getId().toString();

        cli.executeLine("task add " + projectId + " \"Implement parser\" \"RFC 4180\"");
        assertThat(outContent.toString()).contains("Task created:", "Implement parser");

        var task = taskService.listTasks().get(0);
        String taskId = task.getId().toString();

        cli.executeLine("task transition " + taskId + " IN_PROGRESS");
        assertThat(outContent.toString()).contains("transitioned to IN_PROGRESS");

        cli.executeLine("worklog log " + taskId + " 45 --author \"Student\" --desc \"First pass\"");
        assertThat(outContent.toString()).contains("Work logged: 45 min by Student");

        cli.executeLine("task transition " + taskId + " DONE");
        assertThat(outContent.toString()).contains("transitioned to DONE");
    }

    @Test
    void unknownCommandDisplaysClearErrorMessage() {
        cli.executeLine("unknownCommand 123");
        assertThat(errContent.toString()).contains("Unknown command: 'unknowncommand'");
    }

    @Test
    void invalidUuidDisplaysClearErrorMessageWithoutStackTrace() {
        cli.executeLine("project show not-a-uuid");
        assertThat(errContent.toString()).contains("Invalid UUID format: 'not-a-uuid'");
        assertThat(errContent.toString()).doesNotContain("Exception in thread", "at cz.upce");
    }

    @Test
    void interactiveSessionCleanExitOnEof() {
        Scanner scanner = new Scanner("help\nexit\n");
        cli.runInteractive(scanner);
        assertThat(outContent.toString()).contains("TaskPulse CLI", "Available commands:");
    }
}
