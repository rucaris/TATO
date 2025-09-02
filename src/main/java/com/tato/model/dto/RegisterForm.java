package com.tato.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterForm {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;
}
