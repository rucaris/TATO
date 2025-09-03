package com.tato.service;

import com.tato.exception.DuplicateEmailException;
import com.tato.model.User;
import com.tato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(String email, String rawPassword, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword)); // BCrypt 1회
        u.setNickname(nickname);                            // 사용자가 입력한 닉네임
        u.setRole("USER");                                  // 기본 권한
        userRepository.save(u);
    }

    // 컨트롤러에서 쓰는 메서드: 이메일로 사용자 조회
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }
}
