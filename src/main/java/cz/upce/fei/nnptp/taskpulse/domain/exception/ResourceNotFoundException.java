package cz.upce.fei.nnptp.taskpulse.domain.exception;

public class ResourceNotFoundException extends TaskPulseException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
