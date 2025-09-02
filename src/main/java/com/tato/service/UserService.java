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
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String email, String password, String name) {
        System.out.println("=== UserService.register 호출 ===");
        System.out.println("이메일: " + email);
        System.out.println("이름: " + name);

        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            System.out.println("중복 이메일 발견!");
            throw new DuplicateEmailException("이미 가입된 이메일입니다: " + email);
        }

        System.out.println("중복 이메일 없음. 사용자 생성 중...");

        // 사용자 생성 및 저장
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password)) // 비밀번호 암호화
                .name(name)
                .build();

        System.out.println("User 엔티티 생성 완료");

        User savedUser = userRepository.save(user);
        System.out.println("데이터베이스 저장 완료! ID: " + savedUser.getId());

        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
    }
}