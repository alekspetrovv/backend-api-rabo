package nl.rabobank.user;

public interface ITokenGenerator {
    String generateTokenForUser(User user);
}