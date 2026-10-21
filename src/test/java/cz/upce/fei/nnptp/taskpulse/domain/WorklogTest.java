package cz.upce.fei.nnptp.taskpulse.domain;

import cz.upce.fei.nnptp.taskpulse.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorklogTest {

    @Test
    void createValidWorklog() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 21, 10, 0);
        Worklog worklog = new Worklog(time, 45, " Alice ", " Initial work ");

        assertThat(worklog.timestamp()).isEqualTo(time);
        assertThat(worklog.durationMinutes()).isEqualTo(45L);
        assertThat(worklog.author()).isEqualTo("Alice");
        assertThat(worklog.description()).isEqualTo("Initial work");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    void rejectNonPositiveDuration(long invalidDuration) {
        LocalDateTime time = LocalDateTime.now();
        assertThatThrownBy(() -> new Worklog(time, invalidDuration, "Alice"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("positive number of minutes");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void rejectBlankAuthor(String invalidAuthor) {
        LocalDateTime time = LocalDateTime.now();
        assertThatThrownBy(() -> new Worklog(time, 30, invalidAuthor))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Author must not be blank");
    }

    @Test
    void rejectAddingWorklogToNonInProgressTask() {
        UUID projectId = UUID.randomUUID();
        Task todoTask = new Task(projectId, "Task 1");
        Worklog worklog = new Worklog(LocalDateTime.now(), 30, "Alice");

        assertThatThrownBy(() -> todoTask.addWorklog(worklog))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("IN_PROGRESS");

        todoTask.transitionTo(TaskState.IN_PROGRESS);
        todoTask.transitionTo(TaskState.BLOCKED);

        assertThatThrownBy(() -> todoTask.addWorklog(worklog))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("IN_PROGRESS");
    }
}
