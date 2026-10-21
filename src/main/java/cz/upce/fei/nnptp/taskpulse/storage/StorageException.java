package cz.upce.fei.nnptp.taskpulse.storage;

import cz.upce.fei.nnptp.taskpulse.domain.exception.TaskPulseException;

public class StorageException extends TaskPulseException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
