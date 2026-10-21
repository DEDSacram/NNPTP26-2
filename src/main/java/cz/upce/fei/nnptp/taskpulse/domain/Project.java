package cz.upce.fei.nnptp.taskpulse.domain;

import cz.upce.fei.nnptp.taskpulse.domain.exception.ValidationException;

import java.util.Objects;
import java.util.UUID;

public class Project {
    private final UUID id;
    private String name;
    private String description;

    public Project(UUID id, String name, String description) {
        this.id = Objects.requireNonNull(id, "Project ID must not be null");
        setName(name);
        setDescription(description);
    }

    public Project(String name, String description) {
        this(UUID.randomUUID(), name, description);
    }

    public Project(String name) {
        this(name, "");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Project name must not be blank");
        }
        this.name = name.strip();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.strip();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
