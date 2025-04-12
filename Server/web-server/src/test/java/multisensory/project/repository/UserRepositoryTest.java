package multisensory.project.repository;

import multisensory.project.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void UserRepository_FindByName_ReturnUser() {
        User user = new User(UUID.randomUUID(), "Alex", "sahdh23@fja1kz");

        userRepository.save(user);
        User savedUser = userRepository.findByName(user.getName()).get();

        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getName()).isEqualTo(user.getName());
    }
}
