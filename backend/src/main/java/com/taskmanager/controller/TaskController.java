package com.taskmanager.controller;

import com.taskmanager.dto.TaskDtos.TaskRequest;
import com.taskmanager.dto.TaskDtos.TaskResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // le User vient du SecurityContext (cf. JwtAuthenticationFilter), jamais du body :
    // sinon n'importe qui pourrait lire les tâches d'un autre en changeant un id
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getTasks(user));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@AuthenticationPrincipal User user,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal User user, @PathVariable Long id) {
        taskService.deleteTask(user, id);
        return ResponseEntity.noContent().build();
    }
}
