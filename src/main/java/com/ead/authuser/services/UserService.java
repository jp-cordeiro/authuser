package com.ead.authuser.services;

import com.ead.authuser.models.UserModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    List<UserModel> findAllUsers();

    Optional<UserModel> findUserById(UUID userId);

    void delete(UUID userId);

    void save(UserModel userModel);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
