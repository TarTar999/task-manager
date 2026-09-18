package com.taskmanager.service;

import com.taskmanager.dto.TaskDtos.TaskRequest;
import com.taskmanager.dto.TaskDtos.TaskResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ApiException;
import com.taskmanager.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(User user) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional
    public TaskResponse createTask(User user, TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setUser(user);

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(User user, Long taskId, TaskRequest request) {
        Task task = findOwnedTask(user, taskId);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(User user, Long taskId) {
        taskRepository.delete(findOwnedTask(user, taskId));
    }

    /** 404 et pas 403 si la tâche appartient à quelqu'un d'autre : inutile de confirmer qu'elle existe. */
    private Task findOwnedTask(User user, Long taskId) {
        return taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tâche introuvable"));
    }
}
