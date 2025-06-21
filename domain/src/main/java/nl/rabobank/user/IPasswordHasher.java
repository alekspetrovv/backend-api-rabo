package nl.rabobank.user;

public interface IPasswordHasher {
    String hashPassword(String rawPassword);
}