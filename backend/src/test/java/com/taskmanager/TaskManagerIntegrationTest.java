package com.taskmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests de bout en bout sur H2 : on tape les vraies routes HTTP via MockMvc.
// L'ordre compte parce que tous les tests partagent la même base en mémoire.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskManagerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test User","email":"%s","password":"secret123"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    @Test
    @Order(1)
    void registerLoginAndCrudFlow() throws Exception {
        String token = registerAndGetToken("flow@test.com");

        // Connexion
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"flow@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("flow@test.com"));

        // Création d'une tâche
        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Ma première tâche\",\"description\":\"Description test\",\"status\":\"TODO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Ma première tâche"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        long taskId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Liste des tâches
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Filtrage par statut et recherche
        mockMvc.perform(get("/api/tasks")
                        .param("status", "DONE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/tasks")
                        .param("search", "première")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Modification
        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Tâche modifiée\",\"description\":\"MAJ\",\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        // Suppression
        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // et la liste est bien vide derrière
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(2)
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void duplicateEmailReturnsConflict() throws Exception {
        registerAndGetToken("dup@test.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Autre\",\"email\":\"dup@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    void usersCannotAccessOthersTasks() throws Exception {
        String tokenA = registerAndGetToken("usera@test.com");
        String tokenB = registerAndGetToken("userb@test.com");

        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Tâche privée\",\"description\":null,\"status\":\"TODO\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        long taskId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // le coeur du test : B ne doit rien pouvoir faire sur la tâche de A
        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        int status = mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Piratage\",\"status\":\"DONE\"}"))
                .andReturn().getResponse().getStatus();
        assertEquals(404, status);
    }

    // TODO: tester aussi le cas du token expiré, il faudrait pouvoir injecter une
    // expiration courte dans le profil de test
}
