package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTestE2E {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateUserSuccessfully() {
        UserDto dto = new UserDto();
        dto.setUsername("teste");
        dto.setEmail("teste@email.com");
        dto.setPassword("123456");
        dto.setFullName("teste");

        ResponseEntity<UserModel> response =
                restTemplate.postForEntity(
                        "/auth/signup",
                        dto,
                        UserModel.class
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        UserModel createdUser = response.getBody();

        assertNotNull(createdUser.getUserId());
        assertEquals("teste", createdUser.getUsername());
        assertEquals("ACTIVE", createdUser.getUserStatus().name());
        assertEquals("STUDENT", createdUser.getUserType().name());

        var createdUserId = response.getBody().getUserId();
        userRepository.deleteById(createdUserId);
    }

    @Test
    void shouldReturnConflictWhenUsernameExists() {
        UserModel existingUser = new UserModel();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("teste@email.com");
        existingUser.setPassword("123456");
        existingUser.setFullName("teste");
        existingUser.setUserStatus(com.ead.authuser.enums.UserStatus.ACTIVE);
        existingUser.setUserType(com.ead.authuser.enums.UserType.STUDENT);
        existingUser.setCreationDate( java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")) );
        existingUser.setLastUpdateDate( java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")) );
        userRepository.save(existingUser);

        UserDto dto = new UserDto();
        dto.setUsername("existinguser");
        dto.setEmail("teste@email.com");
        dto.setPassword("123456");
        dto.setFullName("teste");

        ResponseEntity<Object> response =
                restTemplate.postForEntity(
                        "/auth/signup",
                        dto,
                       Object.class
                );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Username is already taken", ((Map)response.getBody()).get("error"));

        userRepository.deleteById(existingUser.getUserId());
    }

    @Test
    void shouldReturnConflictWhenEmailExists() {
        UserModel existingUser = new UserModel();
        existingUser.setUsername("username");
        existingUser.setEmail("existingemail@email.com");
        existingUser.setPassword("123456");
        existingUser.setFullName("teste");
        existingUser.setUserStatus(com.ead.authuser.enums.UserStatus.ACTIVE);
        existingUser.setUserType(com.ead.authuser.enums.UserType.STUDENT);
        existingUser.setCreationDate( java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")) );
        existingUser.setLastUpdateDate( java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")) );
        userRepository.save(existingUser);

        UserDto dto = new UserDto();
        dto.setUsername("username2");
        dto.setEmail("existingemail@email.com");
        dto.setPassword("123456");
        dto.setFullName("teste");

        ResponseEntity<Object> response =
                restTemplate.postForEntity(
                        "/auth/signup",
                        dto,
                        Object.class
                );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        //adicione a assert para veririfcar testo do erro da requisição
        assertEquals("Email is already in use", ((Map)response.getBody()).get("error"));

        userRepository.deleteById(existingUser.getUserId());
    }
}
