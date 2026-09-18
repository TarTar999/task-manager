package com.taskmanager.service;

import com.taskmanager.dto.TaskDtos.TaskRequest;
import com.taskmanager.dto.TaskDtos.TaskResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ApiException;
import com.taskmanager.repository.TaskRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private static final Sort RECENT_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(User user, TaskStatus status, String search) {
        // J'ai commencé avec un @Query et des paramètres nullables, c'était vite illisible
        // dès qu'on combine statut + recherche. Les Specifications ajoutent juste les
        // prédicats demandés.
        Specification<Task> spec = ownedBy(user);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    // coalesce sinon les tâches sans description ne matchent jamais
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern)
            ));
        }

        return taskRepository.findAll(spec, RECENT_FIRST)
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

    private Specification<Task> ownedBy(User user) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), user.getId());
    }

    /** 404 et pas 403 si la tâche appartient à quelqu'un d'autre : inutile de confirmer qu'elle existe. */
    private Task findOwnedTask(User user, Long taskId) {
        return taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tâche introuvable"));
    }
}
