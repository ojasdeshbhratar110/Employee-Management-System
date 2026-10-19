package employee_management;

import java.security.Principal;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminUserRepository adminUserRepository;

    public AuthController(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @GetMapping("/me")
    public AuthenticatedAdmin currentAdmin(Principal principal) {
        AdminUser admin = adminUserRepository.findByUsername(principal.getName())
                .orElseThrow();
        return new AuthenticatedAdmin(admin.getUsername(), admin.getFullName());
    }

    public record AuthenticatedAdmin(String username, String fullName) {
    }
}