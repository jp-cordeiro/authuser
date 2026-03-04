package com.ead.authuser.controllers;

import com.ead.authuser.models.UserModel;
import com.ead.authuser.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

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
    void shouldDeleteUserSuccessfully() {
        restTemplate.delete("/users/" + savedUser.getUserId());

        Optional<UserModel> user = userRepository.findById(savedUser.getUserId());

        assertNull(user.orElse(null));
    }
}
