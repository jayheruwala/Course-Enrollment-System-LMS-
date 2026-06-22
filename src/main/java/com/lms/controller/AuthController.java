package com.lms.controller;

import com.lms.dto.JwtResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.entity.Instructor;
import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.exception.BadRequestException;
import com.lms.exception.TokenRefreshException;
import com.lms.repository.InstructorRepository;
import com.lms.repository.StudentRepository;
import com.lms.repository.UserRepository;
import com.lms.security.JwtUtils;
import com.lms.security.UserDetailsImpl;
import com.lms.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Login successful", new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getEmail(), role)));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtTokenFromEmail(user.getEmail());
                    return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", new TokenRefreshResponse(token, requestRefreshToken)));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        String reqRole = signUpRequest.getRole();
        Role role;
        
        if ("INSTRUCTOR".equalsIgnoreCase(reqRole)) {
            role = Role.ROLE_INSTRUCTOR;
            Instructor instructor = new Instructor();
            instructor.setName(signUpRequest.getName());
            instructor.setEmail(signUpRequest.getEmail());
            instructor.setPassword(encoder.encode(signUpRequest.getPassword()));
            instructor.setRole(role);
            instructor.setExpertise("Not Specified");
            instructorRepository.save(instructor);
        } else {
            role = Role.ROLE_STUDENT;
            Student student = new Student();
            student.setName(signUpRequest.getName());
            student.setEmail(signUpRequest.getEmail());
            student.setPassword(encoder.encode(signUpRequest.getPassword()));
            student.setRole(role);
            student.setEnrollmentDate(LocalDate.now());
            student.setStatus("ACTIVE");
            studentRepository.save(student);
        }

        return new ResponseEntity<>(ApiResponse.success("User registered successfully!", null), HttpStatus.CREATED);
    }
}
