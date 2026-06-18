package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import dev.jacksonfishburn.lubelog.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String KEYCLOAK_ID = "11111111-1111-1111-1111-111111111111";
    private static final String EMAIL = "user@example.com";

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void provisionIfAbsent_savesNewUser_whenKeycloakIdNotFound() {
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());

        userService.provisionIfAbsent(KEYCLOAK_ID, EMAIL);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getKeycloakId()).isEqualTo(KEYCLOAK_ID);
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void provisionIfAbsent_doesNothing_whenUserAlreadyExists() {
        User existingUser = User.builder().keycloakId(KEYCLOAK_ID).email(EMAIL).build();
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(existingUser));

        userService.provisionIfAbsent(KEYCLOAK_ID, EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    void provisionIfAbsent_swallowsDataIntegrityViolationException_whenConcurrentInsertRaces() {
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        userService.provisionIfAbsent(KEYCLOAK_ID, EMAIL);
    }
}
