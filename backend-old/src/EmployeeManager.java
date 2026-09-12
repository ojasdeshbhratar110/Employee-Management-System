import java.util.ArrayList;
import java.util.List;

public class EmployeeManager {

    private List<Employee> employees;

    // Constructor
    public EmployeeManager() {
        employees = new ArrayList<>();
    }

    // Add employee
    public void addEmployee(Employee employee) {
        employees.add(employee);
        System.out.println("Employee added successfully.");
    }

    // Get all employees
    public List<Employee> getAllEmployees() {
        return employees;
    }

    // Find employee by ID
    public Employee findEmployeeById(int id) {

        for (Employee employee : employees) {

            if (employee.getId() == id) {
                return employee;
            }
        }

        return null;
    }

    // Delete employee
    public boolean deleteEmployee(int id) {

        Employee employee = findEmployeeById(id);

        if (employee != null) {
            employees.remove(employee);
            return true;
        }

        return false;
    }

    // Update employee
    public boolean updateEmployee(
            int id,
            String name,
            String email,
            String department,
            String designation,
            double salary) {

        Employee employee = findEmployeeById(id);

        if (employee != null) {

            employee.setName(name);
            employee.setEmail(email);
            employee.setDepartment(department);
            employee.setDesignation(designation);
            employee.setSalary(salary);

            return true;
        }

        return false;
    }

    // Search employees by name
    public List<Employee> searchEmployees(String keyword) {

        List<Employee> results = new ArrayList<>();

        for (Employee employee : employees) {

            if (employee.getName()
                    .toLowerCase()
                    .contains(keyword.toLowerCase())) {

                results.add(employee);
            }
        }

        return results;
    }

    // Count employees
    public int getEmployeeCount() {
        return employees.size();
    }

    // Calculate average salary
    public double getAverageSalary() {

        if (employees.isEmpty()) {
            return 0;
        }

        double totalSalary = 0;

        for (Employee employee : employees) {
            totalSalary += employee.getSalary();
        }

        return totalSalary / employees.size();
    }
}
