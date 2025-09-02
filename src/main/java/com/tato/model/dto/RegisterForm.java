package com.tato.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter; @Getter
public class RegisterForm {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String nickname;     // NEW: name → nickname

    @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    public void setEmail(String v){ this.email=v; }
    public void setNickname(String v){ this.nickname=v; }
    public void setPassword(String v){ this.password=v; }
}
