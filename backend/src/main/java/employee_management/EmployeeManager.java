package employee_management;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeManager {

    private final EmployeeRepository employeeRepository;

    public EmployeeManager(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // ADD EMPLOYEE
    public Employee addEmployee(Employee employee) {

        employee.setId(null);

        return employeeRepository.save(employee);
    }

    // GET ALL EMPLOYEES
    public List<Employee> getAllEmployees() {

        return employeeRepository.findAll();
    }

    // FIND EMPLOYEE BY ID
    public Employee findEmployeeById(int id) {

        return employeeRepository
                .findById(id)
                .orElse(null);
    }

    // DELETE EMPLOYEE
    public boolean deleteEmployee(int id) {

        if (!employeeRepository.existsById(id)) {
            return false;
        }

        employeeRepository.deleteById(id);

        return true;
    }

    // UPDATE EMPLOYEE
    public Employee updateEmployee(
            int id,
            Employee updatedEmployee) {

        Employee employee =
                findEmployeeById(id);

        if (employee == null) {
            return null;
        }

        employee.setName(updatedEmployee.getName());
        employee.setEmail(updatedEmployee.getEmail());
        employee.setDepartment(updatedEmployee.getDepartment());
        employee.setDesignation(updatedEmployee.getDesignation());
        employee.setSalary(updatedEmployee.getSalary());
        employee.setActive(updatedEmployee.isActive());

        return employeeRepository.save(employee);
    }

    // SEARCH EMPLOYEES
    public List<Employee> searchEmployees(String keyword) {

        String searchText =
                keyword.toLowerCase();

        return employeeRepository
                .findAll()
                .stream()
                .filter(employee ->

                        employee.getName()
                                .toLowerCase()
                                .contains(searchText)

                        ||

                        employee.getDepartment()
                                .toLowerCase()
                                .contains(searchText)

                        ||

                        employee.getDesignation()
                                .toLowerCase()
                                .contains(searchText)

                )
                .toList();
    }

    // TOTAL EMPLOYEES
    public int getEmployeeCount() {

        return (int) employeeRepository.count();
    }

    // AVERAGE SALARY
    public double getAverageSalary() {

        List<Employee> employees =
                employeeRepository.findAll();

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