package com.biliww.zhuangxiu.service;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import com.biliww.zhuangxiu.domain.User;
import com.biliww.zhuangxiu.dto.AuthResponse;
import com.biliww.zhuangxiu.dto.LoginRequest;
import com.biliww.zhuangxiu.dto.RegisterRequest;
import com.biliww.zhuangxiu.repository.UserRepository;
import com.biliww.zhuangxiu.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        u = userRepository.save(u);
        return toResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return toResponse(u);
    }

    private AuthResponse toResponse(User u) {
        AuthResponse r = new AuthResponse();
        r.setToken(jwtProvider.generate(u.getId()));
        r.setUserId(u.getId());
        r.setNickname(u.getNickname());
        return r;
    }
}
