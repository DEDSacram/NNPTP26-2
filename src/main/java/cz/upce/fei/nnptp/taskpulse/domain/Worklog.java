package cz.upce.fei.nnptp.taskpulse.domain;

import cz.upce.fei.nnptp.taskpulse.domain.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

public record Worklog(
        LocalDateTime timestamp,
        long durationMinutes,
        String author,
        String description
) {
    public Worklog {
        Objects.requireNonNull(timestamp, "Timestamp must not be null");
        if (durationMinutes <= 0) {
            throw new ValidationException("Duration must be a positive number of minutes, got: " + durationMinutes);
        }
        if (author == null || author.isBlank()) {
            throw new ValidationException("Author must not be blank");
        }
        author = author.strip();
        description = description == null ? "" : description.strip();
    }

    public Worklog(LocalDateTime timestamp, long durationMinutes, String author) {
        this(timestamp, durationMinutes, author, "");
    }
}
