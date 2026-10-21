package cz.upce.fei.nnptp.taskpulse.storage;

import cz.upce.fei.nnptp.taskpulse.domain.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryProjectRepository implements ProjectRepository {
    private final Map<UUID, Project> projects = new LinkedHashMap<>();

    @Override
    public Project save(Project project) {
        Objects.requireNonNull(project, "Project must not be null");
        projects.put(project.getId(), project);
        return project;
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return Optional.ofNullable(projects.get(id));
    }

    @Override
    public List<Project> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(projects.values()));
    }

    @Override
    public boolean deleteById(UUID id) {
        return projects.remove(id) != null;
    }

    @Override
    public boolean existsById(UUID id) {
        return projects.containsKey(id);
    }
}
