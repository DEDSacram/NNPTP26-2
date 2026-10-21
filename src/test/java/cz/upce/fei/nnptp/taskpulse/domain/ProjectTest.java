package cz.upce.fei.nnptp.taskpulse.domain;

import cz.upce.fei.nnptp.taskpulse.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    @Test
    void createProjectWithValidNameAndDescription() {
        UUID id = UUID.randomUUID();
        Project project = new Project(id, "Course Project", "TaskPulse development");

        assertThat(project.getId()).isEqualTo(id);
        assertThat(project.getName()).isEqualTo("Course Project");
        assertThat(project.getDescription()).isEqualTo("TaskPulse development");
    }

    @Test
    void createProjectWithConvenienceConstructors() {
        Project projectWithName = new Project("Alpha");
        assertThat(projectWithName.getId()).isNotNull();
        assertThat(projectWithName.getName()).isEqualTo("Alpha");
        assertThat(projectWithName.getDescription()).isEmpty();

        Project projectWithDesc = new Project("Beta", "Beta desc");
        assertThat(projectWithDesc.getId()).isNotNull();
        assertThat(projectWithDesc.getName()).isEqualTo("Beta");
        assertThat(projectWithDesc.getDescription()).isEqualTo("Beta desc");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rejectBlankProjectName(String invalidName) {
        assertThatThrownBy(() -> new Project(invalidName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Project name must not be blank");
    }

    @Test
    void updateProjectDetails() {
        Project project = new Project("Initial", "Initial desc");
        project.setName("Updated");
        project.setDescription("Updated desc");

        assertThat(project.getName()).isEqualTo("Updated");
        assertThat(project.getDescription()).isEqualTo("Updated desc");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rejectUpdatingToBlankProjectName(String invalidName) {
        Project project = new Project("Valid");
        assertThatThrownBy(() -> project.setName(invalidName))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void equalsAndHashCodeBasedOnId() {
        UUID id = UUID.randomUUID();
        Project p1 = new Project(id, "Name 1", "Desc 1");
        Project p2 = new Project(id, "Name 2", "Desc 2");
        Project p3 = new Project(UUID.randomUUID(), "Name 1", "Desc 1");

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        assertThat(p1).isNotEqualTo(p3);
    }
}
