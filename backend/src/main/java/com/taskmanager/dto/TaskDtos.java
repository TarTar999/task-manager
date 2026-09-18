package com.taskmanager.dto;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class TaskDtos {

    private TaskDtos() {
    }

    public record TaskRequest(
            @NotBlank(message = "Le titre est obligatoire")
            @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
            String title,

            String description,

            // null accepté : le service retombe sur TODO
            TaskStatus status
    ) {
    }

    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static TaskResponse from(Task task) {
            return new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getCreatedAt(),
                    task.getUpdatedAt()
            );
        }
    }
}
