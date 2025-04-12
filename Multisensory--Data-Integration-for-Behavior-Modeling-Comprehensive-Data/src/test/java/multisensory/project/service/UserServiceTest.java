package multisensory.project.service;

import jakarta.persistence.EntityNotFoundException;
import multisensory.project.model.User;
import multisensory.project.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @InjectMocks
    private UserService userService;

    @Test
    public void UserService_FindByName_UserExists() {
        User user = new User(UUID.randomUUID(), "Alex", "sahdh23@fja1kz");

        when(userRepository.findByName(Mockito.any(String.class))).thenReturn(Optional.of(user));

        User userFound = userService.findByName(user.getName());

        Assertions.assertThat(userFound).isNotNull();
        Assertions.assertThat(userFound.getName()).isEqualTo(user.getName());
    }

    @Test
    public void UserService_FindByName_UserDoesNotExist() {
        when(userRepository.findByName(Mockito.any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() ->  userService.findByName("Alex"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void UserService_ExistByUsername_UserExists() {
        User user = new User(UUID.randomUUID(), "Alex", "sahdh23@fja1kz");

        when(userRepository.findByName(Mockito.any(String.class)))
                .thenReturn(Optional.of(user));

        Boolean userFound = userService.existsByUsername(user.getName());
    }

    @Test
    public void UserService_UserRegister_ReturnToken() {
        when(userRepository.findByName(Mockito.any(String.class)))
                .thenReturn(Optional.empty());
        when(jwtTokenService.generateToken(Mockito.any(UUID.class)))
                .thenReturn("Bearer 17eh1e1...");

        Assertions.assertThat(userService.userRegister("Alex", "adw2f2@1*"))
                .isNotNull();
        Assertions.assertThat(userService.userRegister("Alex", "adw2f2@1*")
                .length()).isGreaterThan(0);
    }
}
