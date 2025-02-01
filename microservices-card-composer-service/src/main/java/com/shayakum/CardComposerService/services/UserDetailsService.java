package com.shayakum.CardComposerService.services;

import com.shayakum.CardComposerService.models.User;
import com.shayakum.CardComposerService.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UsersRepository usersRepository;

    @Autowired
    public UserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> user = usersRepository.findByLogin(login);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден!");
        }

        return new com.shayakum.CardComposerService.security.UserDetails(user.get());
    }
}
