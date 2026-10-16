package com.rockranger.chat.auth.api;

import com.rockranger.chat.auth.application.RegistrationService;
import com.rockranger.chat.auth.dto.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<RegistrationStartResponse> startRegistration(@Valid @RequestBody RegistrationStartRequest request,
                                                                      HttpServletRequest httpRequest,
                                                                      HttpServletResponse httpResponse) {
        RegistrationStartResponse response = registrationService.startRegistration(request, httpRequest, httpResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/otp/resend")
    public ResponseEntity<Void> resendOtp(@PathVariable("id") UUID id,
                                         @RequestHeader(value = "X-Challenge-Token", required = false) String tokenHeader,
                                         HttpServletRequest httpRequest) {
        String token = resolveChallengeToken(tokenHeader, httpRequest);
        registrationService.resendOtp(id, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/otp/verify")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(@PathVariable("id") UUID id,
                                                      @RequestHeader(value = "X-Challenge-Token", required = false) String tokenHeader,
                                                      @Valid @RequestBody OtpVerifyRequest request,
                                                      HttpServletRequest httpRequest) {
        String token = resolveChallengeToken(tokenHeader, httpRequest);
        OtpVerifyResponse response = registrationService.verifyOtp(id, token, request.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RegistrationCompleteResponse> completeRegistration(@PathVariable("id") UUID id,
                                                                            @RequestHeader(value = "X-Challenge-Token", required = false) String tokenHeader,
                                                                            @Valid @RequestBody RegistrationCompleteRequest request,
                                                                            HttpServletRequest httpRequest) {
        String token = resolveChallengeToken(tokenHeader, httpRequest);
        RegistrationCompleteResponse response = registrationService.completeRegistration(id, token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationStatusResponse> getRegistrationStatus(@PathVariable("id") UUID id,
                                                                            @RequestHeader(value = "X-Challenge-Token", required = false) String tokenHeader,
                                                                            HttpServletRequest httpRequest) {
        String token = resolveChallengeToken(tokenHeader, httpRequest);
        RegistrationStatusResponse response = registrationService.getRegistrationStatus(id, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/username")
    public ResponseEntity<Void> setInitialUsername(@PathVariable("id") UUID id,
                                                   @RequestHeader(value = "X-Challenge-Token", required = false) String tokenHeader,
                                                   @Valid @RequestBody UsernameRequest request,
                                                   HttpServletRequest httpRequest) {
        String token = resolveChallengeToken(tokenHeader, httpRequest);
        registrationService.setInitialUsername(id, token, request.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<AuthResponse> finishRegistration(@PathVariable("id") UUID id,
                                                          @RequestHeader(value = "X-Challenge-Token", required = false) String tokenHeader,
                                                          @RequestBody(required = false) RegistrationFinishRequest request,
                                                          HttpServletRequest httpRequest,
                                                          HttpServletResponse httpResponse) {
        String token = resolveChallengeToken(tokenHeader, httpRequest);
        boolean skipOptional = request == null || request.isSkipOptionalProfile();
        AuthResponse response = registrationService.finishRegistration(id, token, request != null ? request : new RegistrationFinishRequest(), httpResponse);
        return ResponseEntity.ok(response);
    }

    private String resolveChallengeToken(String tokenHeader, HttpServletRequest request) {
        if (tokenHeader != null && !tokenHeader.isBlank()) {
            return tokenHeader.trim();
        }
        if (request != null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("registration_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
