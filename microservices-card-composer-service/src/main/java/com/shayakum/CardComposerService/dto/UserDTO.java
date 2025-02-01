package com.shayakum.CardComposerService.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    @NotEmpty(message = "Поле логина не должно быть пустым!")
    @Size(min = 2, max = 12, message = "Поле логина должно быть длиной от 2 до 12 символов!")
    private String login;

    @NotEmpty(message = "Поле пароля не должно быть пустым!")
    private String password;
}
