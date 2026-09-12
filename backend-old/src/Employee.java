public class Employee {

    private int id;
    private String name;
    private String email;
    private String department;
    private String designation;
    private double salary;
    private boolean active;

    // Constructor
    public Employee(int id, String name, String email,
                    String department, String designation,
                    double salary) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.salary = salary;
        this.active = true;
    }

    // Getters

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public double getSalary() {
        return salary;
    }

    public boolean isActive() {
        return active;
    }

    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Display employee information

    public void displayEmployee() {

        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Department: " + department);
        System.out.println("Designation: " + designation);
        System.out.println("Salary: ₹" + salary);
        System.out.println("Status: " +
                (active ? "Active" : "Inactive"));
    }
}