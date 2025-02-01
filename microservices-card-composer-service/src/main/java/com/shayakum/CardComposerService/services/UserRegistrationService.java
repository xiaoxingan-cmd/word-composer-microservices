package com.shayakum.CardComposerService.services;

import com.shayakum.CardComposerService.models.User;
import com.shayakum.CardComposerService.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserRegistrationService implements UserDetailsService, Validator {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> user = usersRepository.findByLogin(login);

        return user.map(com.shayakum.CardComposerService.security.UserDetails::new).orElse(null);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (loadUserByUsername((String) target) == null) {
            return;
        } else {
            errors.rejectValue("login", "", "Человек с таким логином уже существует!");
        }
    }

    @Transactional(readOnly = false)
    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");

        usersRepository.save(user);
    }
}
