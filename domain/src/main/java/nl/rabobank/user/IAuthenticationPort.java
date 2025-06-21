package nl.rabobank.user;

public interface IAuthenticationPort {
    void authenticate(String username, String password);
}