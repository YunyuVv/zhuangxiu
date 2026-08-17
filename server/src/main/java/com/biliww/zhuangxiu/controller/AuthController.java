package com.biliww.zhuangxiu.controller;

import com.biliww.zhuangxiu.common.ApiResult;
import com.biliww.zhuangxiu.dto.AuthResponse;
import com.biliww.zhuangxiu.dto.LoginRequest;
import com.biliww.zhuangxiu.dto.RegisterRequest;
import com.biliww.zhuangxiu.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResult<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResult.success(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResult<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResult.success(authService.login(req));
    }
}
