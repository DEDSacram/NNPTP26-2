package cz.upce.fei.nnptp.taskpulse.storage;

import cz.upce.fei.nnptp.taskpulse.domain.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryTaskRepository implements TaskRepository {
    private final Map<UUID, Task> tasks = new LinkedHashMap<>();

    @Override
    public Task save(Task task) {
        Objects.requireNonNull(task, "Task must not be null");
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(tasks.values()));
    }

    @Override
    public List<Task> findByProjectId(UUID projectId) {
        Objects.requireNonNull(projectId, "Project ID must not be null");
        return tasks.values().stream()
                .filter(task -> projectId.equals(task.getProjectId()))
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        return tasks.remove(id) != null;
    }

    @Override
    public boolean existsById(UUID id) {
        return tasks.containsKey(id);
    }

    @Override
    public long countByProjectId(UUID projectId) {
        Objects.requireNonNull(projectId, "Project ID must not be null");
        return tasks.values().stream()
                .filter(task -> projectId.equals(task.getProjectId()))
                .count();
    }
}
