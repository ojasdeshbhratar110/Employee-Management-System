package employee_management;

import org.springframework.security.core.AuthenticationException;

public class LoginRateLimitException extends AuthenticationException {

    private final long retryAfterSeconds;

    public LoginRateLimitException(long retryAfterSeconds) {
        super("Too many failed login attempts");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}