const addButton = document.querySelector(".add-btn");
const modal = document.querySelector("#employeeModal");
const closeButton = document.querySelector("#closeModal");

const employeeForm = document.querySelector("#employeeForm");
const employeeTable = document.querySelector("tbody");
const submitButton = document.querySelector("#submitEmployee");

let editingRow = null;


// OPEN ADD EMPLOYEE FORM

addButton.addEventListener("click", function () {

    editingRow = null;

    employeeForm.reset();

    submitButton.innerText = "Add Employee";

    modal.style.display = "flex";
});


// CLOSE FORM

closeButton.addEventListener("click", function () {

    modal.style.display = "none";

});


// ADD / UPDATE EMPLOYEE

employeeForm.addEventListener("submit", function (event) {

    event.preventDefault();

    const name = document.querySelector("#employeeName").value;
    const email = document.querySelector("#employeeEmail").value;
    const department = document.querySelector("#employeeDepartment").value;
    const designation = document.querySelector("#employeeDesignation").value;
    const salary = document.querySelector("#employeeSalary").value;


    // EDIT EXISTING EMPLOYEE

    if (editingRow) {

        editingRow.cells[1].innerText = name;
        editingRow.cells[2].innerText = department;
        editingRow.cells[3].innerText = designation;
        editingRow.cells[4].innerText = "₹" + salary;

        editingRow = null;

        submitButton.innerText = "Add Employee";

    }


    // ADD NEW EMPLOYEE

    else {

        const employeeCount = employeeTable.rows.length + 1;

        const newRow = document.createElement("tr");

        newRow.innerHTML = `
            <td>EMP00${employeeCount}</td>
            <td>${name}</td>
            <td>${department}</td>
            <td>${designation}</td>
            <td>₹${salary}</td>

            <td>
                <button>Edit</button>
                <button>Delete</button>
            </td>
        `;

        employeeTable.appendChild(newRow);
        updateDashboard();

    }


    employeeForm.reset();

    modal.style.display = "none";

});


// EDIT & DELETE BUTTONS

employeeTable.addEventListener("click", function (event) {

    const row = event.target.closest("tr");


    // DELETE

    if (event.target.innerText === "Delete") {

        const confirmDelete = confirm(
            "Are you sure you want to delete this employee?"
        );

        if (confirmDelete) {

            row.remove();
            updateDashboard();

        }

    }


    // EDIT

    if (event.target.innerText === "Edit") {

        editingRow = row;

        document.querySelector("#employeeName").value =
            row.cells[1].innerText;

        document.querySelector("#employeeDepartment").value =
            row.cells[2].innerText;

        document.querySelector("#employeeDesignation").value =
            row.cells[3].innerText;

        document.querySelector("#employeeSalary").value =
            row.cells[4].innerText.replace("₹", "");

        submitButton.innerText = "Update Employee";

        modal.style.display = "flex";

    }

});

const searchInput = document.querySelector("#searchInput");

searchInput.addEventListener("input", function () {

    const searchText = searchInput.value.toLowerCase();

    const rows = employeeTable.querySelectorAll("tr");

    rows.forEach(function (row) {

        const rowText = row.innerText.toLowerCase();

        if (rowText.includes(searchText)) {
            row.style.display = "";
        } else {
            row.style.display = "none";
        }

    });

});
function updateDashboard() {

    const rows = employeeTable.querySelectorAll("tr");

    const totalEmployees = rows.length;

    document.querySelector("#totalEmployees").innerText =
        totalEmployees;

    document.querySelector("#activeEmployees").innerText =
        totalEmployees;

    const departments = new Set();

    rows.forEach(function (row) {

        const department = row.cells[2].innerText;

        departments.add(department);

        let totalSalary = 0;

rows.forEach(function (row) {

    const salaryText = row.cells[4].innerText;

    const salary = Number(
        salaryText.replace("₹", "").replace(",", "")
    );

    totalSalary += salary;

});

let averageSalary = 0;

if (rows.length > 0) {
    averageSalary = totalSalary / rows.length;
}

document.querySelector("#averageSalary").innerText =
    "₹" + Math.round(averageSalary).toLocaleString("en-IN");

    });

    document.querySelector("#totalDepartments").innerText =
        departments.size;
}
updateDashboard();