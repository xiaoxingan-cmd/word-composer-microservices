package com.shayakum.CardComposerService.repositories;

import com.shayakum.CardComposerService.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
}
