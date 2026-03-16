package com.moneyapp.v1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneyapp.v1.model.File;
import com.moneyapp.v1.model.User;

public interface FileRepository extends JpaRepository<File, UUID>{
    List<File> findByUser(User user);
    Optional<File> findByHash(String hash);
    Optional<File> findByIdAndUser(UUID id, User user);
}
