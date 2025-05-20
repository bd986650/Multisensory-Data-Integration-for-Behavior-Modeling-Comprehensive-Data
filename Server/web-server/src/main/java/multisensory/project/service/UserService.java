package multisensory.project.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import multisensory.project.model.User;
import multisensory.project.repository.UserRepository;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

    public User findByName(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("User with this name does not exist"));
    }

    public boolean existsByUsername(String name) {
        return userRepository.findByName(name).isPresent();
    }

    public void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataAccessResourceFailureException e) {
            throw new DataAccessResourceFailureException("DataBase connection Error");
        }
    }
}
