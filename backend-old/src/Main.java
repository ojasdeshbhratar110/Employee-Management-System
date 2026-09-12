public class Main {

    public static void main(String[] args) {

        EmployeeManager manager = new EmployeeManager();

        // Create employees
        Employee employee1 = new Employee(
                1,
                "Rahul Sharma",
                "rahul@gmail.com",
                "IT",
                "Software Developer",
                50000
        );

        Employee employee2 = new Employee(
                2,
                "Priya Singh",
                "priya@gmail.com",
                "HR",
                "HR Manager",
                60000
        );

        // Add employees
        manager.addEmployee(employee1);
        manager.addEmployee(employee2);

        // Display employees
        System.out.println("\n--- Employee List ---");

        for (Employee employee : manager.getAllEmployees()) {
            employee.displayEmployee();
            System.out.println("--------------------");
        }

        // Employee count
        System.out.println(
                "Total Employees: " +
                manager.getEmployeeCount()
        );

        // Average salary
        System.out.println(
                "Average Salary: ₹" +
                manager.getAverageSalary()
        );
    }
}
