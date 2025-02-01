package com.shayakum.CardComposerService.controllers;

import com.shayakum.CardComposerService.dto.UserDTO;
import com.shayakum.CardComposerService.models.User;
import com.shayakum.CardComposerService.security.JWTUtil;
import com.shayakum.CardComposerService.services.UserRegistrationService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRegistrationService userRegistrationService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserRegistrationService userRegistrationService, JWTUtil jwtUtil, ModelMapper modelMapper, AuthenticationManager authenticationManager) {
        this.userRegistrationService = userRegistrationService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody @Valid UserDTO userDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDTO.getLogin(),
                        userDTO.getPassword());
        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException exception) {
            return Map.of("message", "Incorrect credentials!");
        }

        String token = jwtUtil.generateToken(userDTO.getLogin());
        return Map.of("jwt-token", token);
    }

    @PostMapping("/registration")
    public Map<String, String> performRegistration(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        User user = convertToUser(userDTO);

        userRegistrationService.validate(user.getLogin(), bindingResult);
        if (bindingResult.hasErrors()) {
            return Map.of("message", "Error while registration was performing!");
        }

        userRegistrationService.register(user);
        String token = jwtUtil.generateToken(user.getLogin());

        return Map.of("jwt-token", token);
    }

    private User convertToUser(UserDTO userDTO) {
        return this.modelMapper.map(userDTO, User.class);
    }
}
