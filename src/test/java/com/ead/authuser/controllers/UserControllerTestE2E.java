package com.ead.authuser.controllers;

import com.ead.authuser.models.UserModel;
import com.ead.authuser.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTestE2E {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private UserModel savedUser;

    @BeforeEach
    void setup() {
        // garantir ambiente limpo e criar um usuário de teste
        userRepository.deleteAll();

        UserModel newUser = new UserModel();
        newUser.setUsername("username");
        newUser.setEmail("test@email.com");
        newUser.setPassword("123456");
        newUser.setFullName("full name");
        newUser.setUserStatus(com.ead.authuser.enums.UserStatus.ACTIVE);
        newUser.setUserType(com.ead.authuser.enums.UserType.STUDENT);
        newUser.setCreationDate(java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")));
        newUser.setLastUpdateDate(java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")));
        savedUser = userRepository.save(newUser);
    }

    @AfterEach
    void teardown() {
        if (savedUser != null && savedUser.getUserId() != null) {
            userRepository.deleteById(savedUser.getUserId());
        }
    }


    @Test
    void shouldGetUserListSuccessfully() {
        ResponseEntity<UserModel[]> response = restTemplate.getForEntity("/users", UserModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserModel[] users = response.getBody();
        assertEquals(1, users.length);

        UserModel createdUser = users[0];

        assertNotNull(createdUser.getUserId());
        assertEquals("username", createdUser.getUsername());
        assertEquals("ACTIVE", createdUser.getUserStatus().name());
        assertEquals("STUDENT", createdUser.getUserType().name());
    }

    @Test
    void shouldGetOneUserSuccessfully() {
        ResponseEntity<UserModel> response = restTemplate.getForEntity("/users/" + savedUser.getUserId(), UserModel.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserModel user = response.getBody();

        assertNotNull(user.getUserId());
        assertEquals("username", user.getUsername());
        assertEquals("ACTIVE", user.getUserStatus().name());
        assertEquals("STUDENT", user.getUserType().name());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", "updated full name");
        updates.put("cpf", "1234567890");
        updates.put("phoneNumber", "1234567890");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updates, headers);

        ResponseEntity<UserModel> response = restTemplate.exchange("/users/" + savedUser.getUserId(), HttpMethod.PUT, requestEntity, UserModel.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserModel updatedUser = response.getBody();

        assertNotNull(updatedUser.getUserId());
        assertEquals(updatedUser.getUserId(), savedUser.getUserId());
        assertEquals(updates.get("fullName"), updatedUser.getFullName());
        assertEquals(updates.get("cpf"), updatedUser.getCpf());
        assertEquals(updates.get("phoneNumber"), updatedUser.getPhoneNumber());

        Optional<UserModel> fromDb = userRepository.findById(savedUser.getUserId());
        assertTrue(fromDb.isPresent());
        UserModel dbUser = fromDb.get();
        assertEquals(updates.get("fullName"), dbUser.getFullName());
        assertEquals(updates.get("cpf"), dbUser.getCpf());
        assertEquals(updates.get("phoneNumber"), dbUser.getPhoneNumber());

    }


    @Test
    void shouldDeleteUserSuccessfully() {
        restTemplate.delete("/users/" + savedUser.getUserId());

        Optional<UserModel> user = userRepository.findById(savedUser.getUserId());

        assertNull(user.orElse(null));
    }
}
