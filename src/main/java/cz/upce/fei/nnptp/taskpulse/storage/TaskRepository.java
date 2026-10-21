package cz.upce.fei.nnptp.taskpulse.storage;

import cz.upce.fei.nnptp.taskpulse.domain.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(UUID id);
    List<Task> findAll();
    List<Task> findByProjectId(UUID projectId);
    boolean deleteById(UUID id);
    boolean existsById(UUID id);
    long countByProjectId(UUID projectId);
}
