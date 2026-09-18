package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    // toujours passer par cette méthode plutôt que findById : le userId dans la clause
    // where est ce qui empêche un utilisateur de toucher aux tâches d'un autre
    Optional<Task> findByIdAndUserId(Long id, Long userId);
}
