package cz.upce.fei.nnptp.taskpulse.storage;

import cz.upce.fei.nnptp.taskpulse.domain.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Project save(Project project);
    Optional<Project> findById(UUID id);
    List<Project> findAll();
    boolean deleteById(UUID id);
    boolean existsById(UUID id);
}
