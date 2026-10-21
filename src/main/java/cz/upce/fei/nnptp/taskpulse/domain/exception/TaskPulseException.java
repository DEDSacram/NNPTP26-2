package cz.upce.fei.nnptp.taskpulse.domain.exception;

public class TaskPulseException extends RuntimeException {
    public TaskPulseException(String message) {
        super(message);
    }

    public TaskPulseException(String message, Throwable cause) {
        super(message, cause);
    }
}
