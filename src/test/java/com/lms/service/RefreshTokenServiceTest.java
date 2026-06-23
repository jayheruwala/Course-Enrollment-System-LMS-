package com.lms.service;

import com.lms.entity.RefreshToken;
import com.lms.entity.User;
import com.lms.exception.TokenRefreshException;
import com.lms.repository.RefreshTokenRepository;
import com.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L); // 1 day

        user = new User();
        user.setId(1L);

        refreshToken = new RefreshToken();
        refreshToken.setId(10L);
        refreshToken.setUser(user);
        refreshToken.setToken("mock-token-uuid");
        refreshToken.setExpiryDate(Instant.now().plusMillis(86400000L));
    }

    @Test
    void createRefreshToken_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken created = refreshTokenService.createRefreshToken(1L);

        assertNotNull(created);
        assertEquals(user, created.getUser());
        assertNotNull(created.getToken());
        assertTrue(created.getExpiryDate().isAfter(Instant.now()));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_ValidToken() {
        RefreshToken verified = refreshTokenService.verifyExpiration(refreshToken);

        assertNotNull(verified);
        assertEquals("mock-token-uuid", verified.getToken());
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_ExpiredToken_ThrowsException() {
        refreshToken.setExpiryDate(Instant.now().minusMillis(1000L));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyExpiration(refreshToken));
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void findByToken_Success() {
        when(refreshTokenRepository.findByToken("mock-token-uuid")).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> found = refreshTokenService.findByToken("mock-token-uuid");

        assertTrue(found.isPresent());
        assertEquals(refreshToken, found.get());
    }

    @Test
    void deleteByUserId_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.deleteByUser(user)).thenReturn(1);

        int result = refreshTokenService.deleteByUserId(1L);

        assertEquals(1, result);
        verify(refreshTokenRepository).deleteByUser(user);
    }
}
