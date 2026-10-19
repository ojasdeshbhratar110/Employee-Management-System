package employee_management;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminBootstrapRunner(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            Environment environment) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!args.containsOption("bootstrap-admin")) {
            return;
        }

        String username = environment.getProperty("EMS_BOOTSTRAP_USERNAME");
        String password = environment.getProperty("EMS_BOOTSTRAP_PASSWORD");
        String fullName = environment.getProperty("EMS_BOOTSTRAP_FULL_NAME");

        if (!hasText(username) || !hasText(password) || !hasText(fullName) || password.length() < 12) {
            throw new IllegalStateException(
                    "Admin bootstrap requires EMS_BOOTSTRAP_USERNAME, EMS_BOOTSTRAP_PASSWORD (12+ characters), and EMS_BOOTSTRAP_FULL_NAME.");
        }

        if (adminUserRepository.count() != 0) {
            throw new IllegalStateException("Admin bootstrap is only allowed when no administrator exists.");
        }

        adminUserRepository.save(new AdminUser(
                username.trim(),
                passwordEncoder.encode(password),
                fullName.trim()));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}