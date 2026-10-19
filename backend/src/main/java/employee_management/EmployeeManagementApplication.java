package employee_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class EmployeeManagementApplication {

	public static void main(String[] args) {
		boolean bootstrapAdmin = Arrays.asList(args).contains("--bootstrap-admin");
		SpringApplication application = new SpringApplication(EmployeeManagementApplication.class);
		if (bootstrapAdmin) {
			application.setWebApplicationType(WebApplicationType.NONE);
		}

		ConfigurableApplicationContext context = application.run(args);
		if (bootstrapAdmin) {
			context.close();
		}
	}

}
