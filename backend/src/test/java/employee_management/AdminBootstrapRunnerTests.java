package employee_management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminBootstrapRunnerTests {

    private static final String TEST_PASSWORD = "test-bootstrap-only-123";

    private final AdminUserRepository adminUserRepository = org.mockito.Mockito.mock(AdminUserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @Test
    void doesNothingUnlessExplicitlyRequested() throws Exception {
        AdminBootstrapRunner runner = runner(new MockEnvironment());

        runner.run(new DefaultApplicationArguments("--ordinary-start"));

        verify(adminUserRepository, never()).count();
        verify(adminUserRepository, never()).save(any(AdminUser.class));
    }

    @Test
    void refusesBootstrapWhenCredentialsAreMissing() {
        AdminBootstrapRunner runner = runner(new MockEnvironment());

        assertThatThrownBy(() -> runner.run(bootstrapArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires EMS_BOOTSTRAP_USERNAME");
        verify(adminUserRepository, never()).save(any(AdminUser.class));
    }

    @Test
    void refusesToCreateAnAdditionalAdministrator() {
        MockEnvironment environment = credentials();
        when(adminUserRepository.count()).thenReturn(1L);

        assertThatThrownBy(() -> runner(environment).run(bootstrapArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only allowed when no administrator exists");
        verify(adminUserRepository, never()).save(any(AdminUser.class));
    }

    @Test
    void createsOneAccountWithOnlyAHashedPasswordWhenExplicitlyRequested() {
        MockEnvironment environment = credentials();
        when(adminUserRepository.count()).thenReturn(0L);
        when(adminUserRepository.save(any(AdminUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        runner(environment).run(bootstrapArguments());

        var savedAdmin = forClass(AdminUser.class);
        verify(adminUserRepository).save(savedAdmin.capture());
        assertThat(savedAdmin.getValue().getUsername()).isEqualTo("bootstrap-test-admin");
        assertThat(savedAdmin.getValue().getPasswordHash()).isNotEqualTo(TEST_PASSWORD);
        assertThat(passwordEncoder.matches(TEST_PASSWORD, savedAdmin.getValue().getPasswordHash())).isTrue();
    }

    private AdminBootstrapRunner runner(MockEnvironment environment) {
        return new AdminBootstrapRunner(adminUserRepository, passwordEncoder, environment);
    }

    private MockEnvironment credentials() {
        return new MockEnvironment()
                .withProperty("EMS_BOOTSTRAP_USERNAME", "bootstrap-test-admin")
                .withProperty("EMS_BOOTSTRAP_PASSWORD", TEST_PASSWORD)
                .withProperty("EMS_BOOTSTRAP_FULL_NAME", "Bootstrap Test Administrator");
    }

    private DefaultApplicationArguments bootstrapArguments() {
        return new DefaultApplicationArguments("--bootstrap-admin");
    }
}