package employee_management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.ServletContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.TestPropertySource;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:ems-security;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class EmployeeManagementApplicationTests {

	private static final String TEST_PASSWORD = "test-only-password-123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AdminUserRepository adminUserRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Autowired
	private ServletContext servletContext;

	@BeforeEach
	void createTestAdministrator() {
		loginAttemptService.clear();
		adminUserRepository.deleteAll();
		adminUserRepository.save(new AdminUser(
				"test-admin",
				passwordEncoder.encode(TEST_PASSWORD),
				"Test Administrator"));
	}

	@Test
	void storesAdministratorPasswordAsBcryptHash() {
		AdminUser admin = adminUserRepository.findByUsername("test-admin").orElseThrow();

		assertThat(admin.getPasswordHash()).startsWith("$2");
		assertThat(admin.getPasswordHash()).isNotEqualTo(TEST_PASSWORD);
		assertThat(passwordEncoder.matches(TEST_PASSWORD, admin.getPasswordHash())).isTrue();
	}

	@Test
	void csrfEndpointProvidesSecureReadableTokenCookie() throws Exception {
		mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("XSRF-TOKEN"))
				.andExpect(cookie().secure("XSRF-TOKEN", true))
				.andExpect(cookie().httpOnly("XSRF-TOKEN", false));
	}

	@Test
	void loginRequiresCsrfTokenAndCreatesAdministratorSession() throws Exception {
		       MockHttpSession preAuthenticationSession = new MockHttpSession();
		       String oldSessionId = preAuthenticationSession.getId();
		       MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf").session(preAuthenticationSession))
				.andExpect(status().isOk())
				.andReturn();
		var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

		mockMvc.perform(post("/api/auth/login")
					.param("username", "test-admin")
					.param("password", TEST_PASSWORD))
				.andExpect(status().isForbidden());

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
									   .session(preAuthenticationSession)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfCookie.getValue())
					.param("username", "test-admin")
					.param("password", TEST_PASSWORD))
				.andExpect(status().isNoContent())
				.andReturn();

		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
		assertThat(session).isNotNull();
			   assertThat(session.getId()).isNotEqualTo(oldSessionId);

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
								.andExpect(MockMvcResultMatchers.jsonPath("$.username").value("test-admin"))
								.andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value("Test Administrator"))
								.andExpect(MockMvcResultMatchers.jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void invalidCredentialsAreRejected() throws Exception {
		MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andReturn();
		var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

		mockMvc.perform(post("/api/auth/login")
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfCookie.getValue())
					.param("username", "test-admin")
					.param("password", "wrong-test-password"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void throttlesLoginAfterFiveFailuresAndReturnsRetryAfter() throws Exception {
		MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andReturn();
		var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

		for (int attempt = 0; attempt < LoginAttemptService.MAX_FAILED_ATTEMPTS; attempt++) {
			mockMvc.perform(post("/api/auth/login")
						.cookie(csrfCookie)
						.header("X-XSRF-TOKEN", csrfCookie.getValue())
						.param("username", "test-admin")
						.param("password", "incorrect-password"))
					.andExpect(status().isUnauthorized());
		}

		mockMvc.perform(post("/api/auth/login")
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfCookie.getValue())
					.param("username", "test-admin")
					.param("password", TEST_PASSWORD))
				.andExpect(status().isTooManyRequests())
				.andExpect(result -> assertThat(Long.parseLong(
						result.getResponse().getHeader("Retry-After"))).isPositive());
	}

	@Test
	void configuresFifteenMinuteSessionInactivityTimeout() {
		assertThat(servletContext.getSessionTimeout()).isEqualTo(15);
	}

	@Test
	void logoutInvalidatesAuthenticatedSession() throws Exception {
		MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andReturn();
		var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfCookie.getValue())
					.param("username", "test-admin")
					.param("password", TEST_PASSWORD))
				.andExpect(status().isNoContent())
				.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(post("/api/auth/logout")
					.session(session)
					.cookie(csrfCookie)
					.header("X-XSRF-TOKEN", csrfCookie.getValue()))
				.andExpect(status().isNoContent());

		assertThat(session.isInvalid()).isTrue();
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void identityEndpointIsNotPublic() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	       @Test
	       void everyEmployeeRouteRequiresAdministratorSession() throws Exception {
			       MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
					       .andExpect(status().isOk())
					       .andReturn();
			       var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

		       mockMvc.perform(get("/api/employees"))
				       .andExpect(status().isUnauthorized());
		       mockMvc.perform(get("/api/employees/1"))
				       .andExpect(status().isUnauthorized());
		       mockMvc.perform(get("/api/employees/search").param("keyword", "test"))
				       .andExpect(status().isUnauthorized());
			   mockMvc.perform(post("/api/employees")
											   .cookie(csrfCookie)
											   .header("X-XSRF-TOKEN", csrfCookie.getValue())
											   .contentType("application/json")
											   .content("{}"))
				       .andExpect(status().isUnauthorized());
			   mockMvc.perform(put("/api/employees/1")
											   .cookie(csrfCookie)
											   .header("X-XSRF-TOKEN", csrfCookie.getValue())
											   .contentType("application/json")
											   .content("{}"))
				       .andExpect(status().isUnauthorized());
			   mockMvc.perform(delete("/api/employees/1")
											   .cookie(csrfCookie)
											   .header("X-XSRF-TOKEN", csrfCookie.getValue()))
				       .andExpect(status().isUnauthorized());
	       }

	       @Test
	       void employeeWritesRequireValidCsrfToken() throws Exception {
		       MockHttpSession session = authenticatedSession();

		       mockMvc.perform(post("/api/employees")
					       .session(session)
					       .contentType("application/json")
					       .content("{}"))
				       .andExpect(status().isForbidden());
	       }

	       @Test
	       void employeeCreateRejectsInvalidData() throws Exception {
		       MockHttpSession session = authenticatedSession();
		       MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
				       .andExpect(status().isOk())
				       .andReturn();
		       var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

		       mockMvc.perform(post("/api/employees")
					       .session(session)
					       .cookie(csrfCookie)
					       .header("X-XSRF-TOKEN", csrfCookie.getValue())
					       .contentType("application/json")
					       .content("{}"))
				       .andExpect(status().isBadRequest());
	       }

		       @Test
		       void authenticatedEmployeeCrudRetainsExistingJsonContract() throws Exception {
			       MockHttpSession session = authenticatedSession();
			       MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
					       .andExpect(status().isOk())
					       .andReturn();
			       var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
			       String employeeJson = "{\"name\":\"Casey Example\",\"email\":\"casey@example.com\","
					       + "\"department\":\"IT\",\"designation\":\"Engineer\","
					       + "\"salary\":75000,\"active\":true}";

			       MvcResult createResult = mockMvc.perform(post("/api/employees")
						       .session(session)
						       .cookie(csrfCookie)
						       .header("X-XSRF-TOKEN", csrfCookie.getValue())
						       .contentType("application/json")
						       .content(employeeJson))
					       .andExpect(status().isCreated())
					       .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Casey Example"))
					       .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(75000))
					       .andReturn();
			       int employeeId = com.jayway.jsonpath.JsonPath.read(
					       createResult.getResponse().getContentAsString(), "$.id");

			       mockMvc.perform(get("/api/employees/{id}", employeeId).session(session))
					       .andExpect(status().isOk())
					       .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("casey@example.com"));

			       String updatedJson = employeeJson.replace("Casey Example", "Casey Updated");
			       mockMvc.perform(put("/api/employees/{id}", employeeId)
						       .session(session)
						       .cookie(csrfCookie)
						       .header("X-XSRF-TOKEN", csrfCookie.getValue())
						       .contentType("application/json")
						       .content(updatedJson))
					       .andExpect(status().isOk())
					       .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Casey Updated"));

			       mockMvc.perform(delete("/api/employees/{id}", employeeId)
						       .session(session)
						       .cookie(csrfCookie)
						       .header("X-XSRF-TOKEN", csrfCookie.getValue()))
					       .andExpect(status().isNoContent());
		       }

	       private MockHttpSession authenticatedSession() throws Exception {
		       MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
				       .andExpect(status().isOk())
				       .andReturn();
		       var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
		       MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
					       .cookie(csrfCookie)
					       .header("X-XSRF-TOKEN", csrfCookie.getValue())
					       .param("username", "test-admin")
					       .param("password", TEST_PASSWORD))
				       .andExpect(status().isNoContent())
				       .andReturn();
		       return (MockHttpSession) loginResult.getRequest().getSession(false);
	       }

}
