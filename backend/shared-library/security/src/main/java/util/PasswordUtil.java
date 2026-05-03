package util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordUtil(PasswordEncoder encoder) {
        this.passwordEncoder = encoder;
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean authenticate(String rawPassword, String storedEncodedPassword) {
        return passwordEncoder.matches(rawPassword, storedEncodedPassword);
    }
}
