package employee_management;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class RateLimitingAuthenticationProvider implements AuthenticationProvider {

    private final DaoAuthenticationProvider delegate;
    private final LoginAttemptService loginAttemptService;

    public RateLimitingAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            LoginAttemptService loginAttemptService) {
        this.delegate = new DaoAuthenticationProvider(userDetailsService);
        this.delegate.setPasswordEncoder(passwordEncoder);
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        long retryAfterSeconds = loginAttemptService.retryAfterSeconds(username);
        if (retryAfterSeconds > 0) {
            throw new LoginRateLimitException(retryAfterSeconds);
        }

        try {
            Authentication authenticated = delegate.authenticate(authentication);
            loginAttemptService.recordSuccess(username);
            return authenticated;
        } catch (AuthenticationException exception) {
            loginAttemptService.recordFailure(username);
            throw exception;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
}