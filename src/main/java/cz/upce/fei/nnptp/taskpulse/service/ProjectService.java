package cz.upce.fei.nnptp.taskpulse.service;

import cz.upce.fei.nnptp.taskpulse.domain.Project;
import cz.upce.fei.nnptp.taskpulse.domain.exception.ResourceNotFoundException;
import cz.upce.fei.nnptp.taskpulse.domain.exception.ValidationException;
import cz.upce.fei.nnptp.taskpulse.storage.ProjectRepository;
import cz.upce.fei.nnptp.taskpulse.storage.TaskRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = Objects.requireNonNull(projectRepository, "ProjectRepository must not be null");
        this.taskRepository = Objects.requireNonNull(taskRepository, "TaskRepository must not be null");
    }

    public Project createProject(String name, String description) {
        Project project = new Project(name, description);
        return projectRepository.save(project);
    }

    public Project getProject(UUID id) {
        Objects.requireNonNull(id, "Project ID must not be null");
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    public List<Project> listProjects() {
        return projectRepository.findAll();
    }

    public Project updateProject(UUID id, String newName, String newDescription) {
        Project project = getProject(id);
        project.setName(newName);
        project.setDescription(newDescription);
        return projectRepository.save(project);
    }

    public void deleteProject(UUID id) {
        Project project = getProject(id);
        long taskCount = taskRepository.countByProjectId(project.getId());
        if (taskCount > 0) {
            throw new ValidationException("Cannot delete project with id " + id + " because it contains " + taskCount + " task(s)");
        }
        projectRepository.deleteById(id);
    }
}
