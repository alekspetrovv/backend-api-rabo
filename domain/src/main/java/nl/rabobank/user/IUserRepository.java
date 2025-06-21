package nl.rabobank.user;

import java.util.Optional;

public interface IUserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
}