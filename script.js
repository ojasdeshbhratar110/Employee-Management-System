if (sessionStorage.getItem("emsLoggedIn") !== "true") {
    window.location.href = "login.html";
}

const API_URL = "https://employee-management-system-puya.onrender.com/api/employees";


const addButton = document.querySelector(".add-btn");
const employeesAddButton = document.querySelector("#employeesAddButton");

const modal = document.querySelector("#employeeModal");
const closeButton = document.querySelector("#closeModal");

const employeeForm = document.querySelector("#employeeForm");
const employeeTable = document.querySelector("#employeesView tbody");
const submitButton = document.querySelector("#submitEmployee");

const searchInput = document.querySelector("#searchInput");
const departmentFilter =
    document.querySelector("#departmentFilter");

const statusFilter =
    document.querySelector("#statusFilter");

const employeeDirectoryCount =
    document.querySelector("#employeeDirectoryCount");
const systemDot =
    document.querySelector("#systemDot");

const systemStatusTitle =
    document.querySelector("#systemStatusTitle");

const systemStatusText =
    document.querySelector("#systemStatusText");


function setSystemStatus(status) {

    if (status === "online") {

        systemStatusTitle.innerText =
            "System Online";

        systemStatusText.innerText =
            "All services operational";

        systemDot.style.backgroundColor =
            "#22c55e";

    } else if (status === "offline") {

        systemStatusTitle.innerText =
            "System Offline";

        systemStatusText.innerText =
            "Backend connection unavailable";

        systemDot.style.backgroundColor =
            "#ef4444";

    } else {

        systemStatusTitle.innerText =
            "Checking...";

        systemStatusText.innerText =
            "Connecting to backend";

        systemDot.style.backgroundColor =
            "#f59e0b";
    }
}

/* =========================================================
   NAVIGATION
   ========================================================= */

const navLinks =
    document.querySelectorAll(".sidebar nav a");

const dashboardNav =
    document.querySelector("#dashboardNav");

const employeesNav =
    document.querySelector("#employeesNav");

const departmentsNav =
    document.querySelector("#departmentsNav");

const reportsNav =
    document.querySelector("#reportsNav");

const settingsNav =
    document.querySelector("#settingsNav");


/* =========================================================
   VIEWS
   ========================================================= */

const dashboardView =
    document.querySelector("#dashboardView");

const employeesView =
    document.querySelector("#employeesView");

const departmentsView =
    document.querySelector("#departmentsView");

const reportsView =
    document.querySelector("#reportsView");

const settingsView =
    document.querySelector("#settingsView");


const viewAllEmployees =
    document.querySelector("#viewAllEmployees");


let employees = [];

let editingEmployeeId = null;


/* =========================================================
   LOAD EMPLOYEES
   ========================================================= */

async function loadEmployees() {

    setSystemStatus("checking");

    try {

        const response =
            await fetch(API_URL);


        if (!response.ok) {

            throw new Error(
                "Could not load employees"
            );
        }


        employees =
            await response.json();


        setSystemStatus("online");


        renderEmployees(employees);

        updateDashboard();

        renderDepartments();

        renderDashboardEmployees();

        renderDashboardDepartments();

        renderReports();


    } catch (error) {

        console.error(error);

        setSystemStatus("offline");


        employees = [];


        renderEmployees(employees);

        updateDashboard();

        renderDepartments();

        renderDashboardEmployees();

        renderDashboardDepartments();

        renderReports();
    }
}

       

        


/* =========================================================
   EMPLOYEE TABLE
   ========================================================= */
   function renderEmployees(employeeList) {

    employeeTable.innerHTML = "";

    if (employeeDirectoryCount) {
        employeeDirectoryCount.innerText = employeeList.length;
    }

    employeeList.forEach(function (employee) {

        const initials = employee.name
            .split(" ")
            .map(function (word) {
                return word.charAt(0);
            })
            .join("")
            .substring(0, 2)
            .toUpperCase();

        const row = document.createElement("tr");

        row.dataset.id = employee.id;

        row.innerHTML = `
            <td>
                <div class="employee-profile">
                    <div class="employee-avatar">
                        ${initials}
                    </div>

                    <div class="employee-info">
                        <strong>${employee.name}</strong>
                        <span>${employee.email}</span>
                    </div>
                </div>
            </td>

            <td>
                <span class="employee-department">
                    ${employee.department}
                </span>
            </td>

            <td>
                ${employee.designation}
            </td>

            <td>
                <span class="${
                    employee.active
                        ? "status-active"
                        : "status-inactive"
                }">
                    ${
                        employee.active
                            ? "● Active"
                            : "● Inactive"
                    }
                </span>
            </td>

            <td>
                ₹${Number(employee.salary).toLocaleString("en-IN")}
            </td>

            <td>
                <button class="edit-btn">
                    Edit
                </button>

                <button class="delete-btn">
                    Delete
                </button>
            </td>
        `;

        employeeTable.appendChild(row);
    });


    if (employeeList.length === 0) {

        employeeTable.innerHTML = `
            <tr>
                <td colspan="6"
                    style="text-align:center; padding:35px; color:#8b8d99;">
                    No employees found
                </td>
            </tr>
        `;
    }
}

function applyEmployeeFilters() {

    const searchText =
        searchInput.value
            .toLowerCase()
            .trim();


    const selectedDepartment =
        departmentFilter.value;


    const selectedStatus =
        statusFilter.value;


    const filteredEmployees =
        employees.filter(
            function (employee) {

                const matchesSearch =

                    employee.name
                        .toLowerCase()
                        .includes(searchText)

                    ||

                    employee.email
                        .toLowerCase()
                        .includes(searchText)

                    ||

                    employee.department
                        .toLowerCase()
                        .includes(searchText)

                    ||

                    employee.designation
                        .toLowerCase()
                        .includes(searchText);


                const matchesDepartment =

                    selectedDepartment === "all"

                    ||

                    employee.department ===
                        selectedDepartment;


                const matchesStatus =

                    selectedStatus === "all"

                    ||

                    (
                        selectedStatus === "active"
                        &&
                        employee.active
                    )

                    ||

                    (
                        selectedStatus === "inactive"
                        &&
                        !employee.active
                    );


                return (
                    matchesSearch &&
                    matchesDepartment &&
                    matchesStatus
                );
            }
        );


    renderEmployees(filteredEmployees);
}


searchInput.addEventListener(
    "input",
    applyEmployeeFilters
);


departmentFilter.addEventListener(
    "change",
    applyEmployeeFilters
);


statusFilter.addEventListener(
    "change",
    applyEmployeeFilters
);


/* =========================================================
   OPEN ADD EMPLOYEE
   ========================================================= */

function openAddEmployeeModal() {

    editingEmployeeId = null;

    employeeForm.reset();

    submitButton.innerText =
        "Add Employee";

    document.querySelector(
        ".modal-header h2"
    ).innerText =
        "Add New Employee";

    modal.style.display =
        "flex";
}


addButton.addEventListener(
    "click",
    openAddEmployeeModal
);


employeesAddButton.addEventListener(
    "click",
    openAddEmployeeModal
);


/* =========================================================
   CLOSE MODAL
   ========================================================= */

closeButton.addEventListener(
    "click",
    function () {

        modal.style.display =
            "none";
    }
);


window.addEventListener(
    "click",
    function (event) {

        if (event.target === modal) {

            modal.style.display =
                "none";
        }
    }
);


/* =========================================================
   ADD / UPDATE EMPLOYEE
   ========================================================= */

employeeForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const employeeData = {

            name:
                document.querySelector(
                    "#employeeName"
                ).value,

            email:
                document.querySelector(
                    "#employeeEmail"
                ).value,

            department:
                document.querySelector(
                    "#employeeDepartment"
                ).value,

            designation:
                document.querySelector(
                    "#employeeDesignation"
                ).value,

            salary:
                Number(
                    document.querySelector(
                        "#employeeSalary"
                    ).value
                ),

            active: true
        };


        try {


            if (
                editingEmployeeId !== null
            ) {


                const response =
                    await fetch(

                        `${API_URL}/${editingEmployeeId}`,

                        {
                            method: "PUT",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(
                                    employeeData
                                )
                        }
                    );


                if (!response.ok) {

                    throw new Error(
                        "Could not update employee"
                    );
                }

            } else {


                const response =
                    await fetch(

                        API_URL,

                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(
                                    employeeData
                                )
                        }
                    );


                if (!response.ok) {

                    throw new Error(
                        "Could not add employee"
                    );
                }
            }


            employeeForm.reset();

            modal.style.display =
                "none";

            editingEmployeeId =
                null;

            submitButton.innerText =
                "Add Employee";


            await loadEmployees();


        } catch (error) {

            console.error(error);

            alert(
                "Something went wrong while saving the employee."
            );
        }
    }
);


/* =========================================================
   EDIT / DELETE
   ========================================================= */

employeeTable.addEventListener(
    "click",
    async function (event) {


        const row =
            event.target.closest("tr");


        if (!row) {
            return;
        }


        const employeeId =
            Number(row.dataset.id);



        /* DELETE */

        if (
            event.target.classList.contains(
                "delete-btn"
            )
        ) {


            const confirmDelete =
                confirm(
                    "Are you sure you want to delete this employee?"
                );


            if (!confirmDelete) {
                return;
            }


            try {

                const response =
                    await fetch(

                        `${API_URL}/${employeeId}`,

                        {
                            method: "DELETE"
                        }
                    );


                if (!response.ok) {

                    throw new Error(
                        "Could not delete employee"
                    );
                }


                await loadEmployees();


            } catch (error) {

                console.error(error);

                alert(
                    "Could not delete employee."
                );
            }
        }



        /* EDIT */

        if (
            event.target.classList.contains(
                "edit-btn"
            )
        ) {


            const employee =
                employees.find(
                    function (employee) {

                        return (
                            employee.id ===
                            employeeId
                        );
                    }
                );


            if (!employee) {
                return;
            }


            editingEmployeeId =
                employee.id;


            document.querySelector(
                "#employeeName"
            ).value =
                employee.name;


            document.querySelector(
                "#employeeEmail"
            ).value =
                employee.email;


            document.querySelector(
                "#employeeDepartment"
            ).value =
                employee.department;


            document.querySelector(
                "#employeeDesignation"
            ).value =
                employee.designation;


            document.querySelector(
                "#employeeSalary"
            ).value =
                employee.salary;


            document.querySelector(
                ".modal-header h2"
            ).innerText =
                "Edit Employee";


            submitButton.innerText =
                "Update Employee";


            modal.style.display =
                "flex";
        }
    }
);


/* =========================================================
   SEARCH
   ========================================================= */

function applyEmployeeFilters() {

    const searchText =
        searchInput.value
            .toLowerCase()
            .trim();

    const selectedDepartment =
        departmentFilter.value;

    const selectedStatus =
        statusFilter.value;


    const filteredEmployees =
        employees.filter(
            function (employee) {

                const matchesSearch =
                    employee.name
                        .toLowerCase()
                        .includes(searchText)

                    ||

                    employee.email
                        .toLowerCase()
                        .includes(searchText)

                    ||

                    employee.department
                        .toLowerCase()
                        .includes(searchText)

                    ||

                    employee.designation
                        .toLowerCase()
                        .includes(searchText);


                const matchesDepartment =
                    selectedDepartment === "all"
                    ||
                    employee.department === selectedDepartment;


                const matchesStatus =
                    selectedStatus === "all"

                    ||

                    (
                        selectedStatus === "active"
                        &&
                        employee.active
                    )

                    ||

                    (
                        selectedStatus === "inactive"
                        &&
                        !employee.active
                    );


                return (
                    matchesSearch &&
                    matchesDepartment &&
                    matchesStatus
                );
            }
        );


    renderEmployees(filteredEmployees);
}


searchInput.addEventListener(
    "input",
    applyEmployeeFilters
);

departmentFilter.addEventListener(
    "change",
    applyEmployeeFilters
);

statusFilter.addEventListener(
    "change",
    applyEmployeeFilters
);


/* =========================================================
   DASHBOARD STATS
   ========================================================= */

function updateDashboard() {


    const totalEmployees =
        employees.length;


    document.querySelector(
        "#totalEmployees"
    ).innerText =
        totalEmployees;



    const activeEmployees =
        employees.filter(
            function (employee) {

                return employee.active;
            }
        ).length;


    document.querySelector(
        "#activeEmployees"
    ).innerText =
        activeEmployees;



    const departments =
        new Set();


    employees.forEach(
        function (employee) {

            departments.add(
                employee.department
            );
        }
    );


    document.querySelector(
        "#totalDepartments"
    ).innerText =
        departments.size;



    let totalSalary = 0;


    employees.forEach(
        function (employee) {

            totalSalary +=
                Number(employee.salary);
        }
    );


    let averageSalary = 0;


    if (employees.length > 0) {

        averageSalary =
            totalSalary /
            employees.length;
    }


    document.querySelector(
        "#averageSalary"
    ).innerText =

        "₹" +

        Math.round(
            averageSalary
        ).toLocaleString("en-IN");
}


/* =========================================================
   DEPARTMENTS PAGE
   ========================================================= */

function renderDepartments() {


    const departmentList =
        document.querySelector(
            "#departmentList"
        );


    if (!departmentList) {
        return;
    }


    const departmentCounts = {};
const departmentPageTotal =
    document.querySelector("#departmentPageTotal");

const departmentPageEmployees =
    document.querySelector("#departmentPageEmployees");

const departmentPageActive =
    document.querySelector("#departmentPageActive");

    employees.forEach(
        function (employee) {


            const department =
                employee.department;


            if (
                departmentCounts[
                    department
                ]
            ) {

                departmentCounts[
                    department
                ]++;

            } else {

                departmentCounts[
                    department
                ] = 1;
            }
        }
    );
if (departmentPageTotal) {
    departmentPageTotal.innerText =
        Object.keys(departmentCounts).length;
}

if (departmentPageEmployees) {
    departmentPageEmployees.innerText =
        employees.length;
}

if (departmentPageActive) {
    departmentPageActive.innerText =
        employees.filter(function (employee) {
            return employee.active;
        }).length;
}

    departmentList.innerHTML = "";


    Object.keys(
        departmentCounts
    ).forEach(
        function (department) {


            const departmentCard =
                document.createElement(
                    "div"
                );


            departmentCard.className =
                "card";


            const count =
                departmentCounts[
                    department
                ];


            const percentage =
                employees.length === 0
                    ? 0
                    : Math.round(
                        (
                            count /
                            employees.length
                        ) * 100
                    );


            departmentCard.innerHTML = `

                <div class="department-card-top">

                    <h3>
                        ${department}
                    </h3>

                    <span>
                        ${percentage}%
                    </span>

                </div>


                <p>

                    ${count}

                    Employee${count !== 1 ? "s" : ""}

                </p>


                <div class="department-card-meta">

                    ${count} of ${employees.length}
                    workforce members

                </div>
            `;


            departmentList.appendChild(
                departmentCard
            );
        }
    );
}


/* =========================================================
   DASHBOARD EMPLOYEES
   ========================================================= */

function renderDashboardEmployees() {


    const container =
        document.querySelector(
            "#dashboardEmployeeList"
        );


    if (!container) {
        return;
    }


    container.innerHTML = "";


    const recentEmployees =
        employees
            .slice(-4)
            .reverse();


    recentEmployees.forEach(
        function (employee) {


            const initials =
                employee.name

                    .split(" ")

                    .map(
                        function (word) {

                            return word.charAt(0);
                        }
                    )

                    .join("")

                    .substring(0, 2)

                    .toUpperCase();


            const item =
                document.createElement(
                    "div"
                );


            item.className =
                "dashboard-employee-item";


            item.innerHTML = `

                <div class="employee-profile">

                    <div class="employee-avatar">

                        ${initials}

                    </div>


                    <div class="employee-info">

                        <strong>

                            ${employee.name}

                        </strong>


                        <span>

                            ${employee.designation}

                        </span>

                    </div>

                </div>


                <div class="employee-department">

                    ${employee.department}

                </div>


                <div class="employee-status">

                    <span class="status-dot"></span>

                    ${
                        employee.active
                            ? "Active"
                            : "Inactive"
                    }

                </div>


                <div class="employee-salary">

                    ₹${Number(
                        employee.salary
                    ).toLocaleString("en-IN")}

                </div>
            `;


            container.appendChild(
                item
            );
        }
    );


    if (
        recentEmployees.length === 0
    ) {

        container.innerHTML = `

            <div class="empty-state">

                No employees available

            </div>
        `;
    }
}


/* =========================================================
   DASHBOARD DEPARTMENTS
   ========================================================= */

function renderDashboardDepartments() {


    const container =
        document.querySelector(
            "#dashboardDepartmentList"
        );


    if (!container) {
        return;
    }


    const departmentCounts = {};


    employees.forEach(
        function (employee) {


            const department =
                employee.department;


            if (
                departmentCounts[
                    department
                ]
            ) {

                departmentCounts[
                    department
                ]++;

            } else {

                departmentCounts[
                    department
                ] = 1;
            }
        }
    );


    container.innerHTML = "";


    const totalEmployees =
        employees.length;


    if (totalEmployees === 0) {

        container.innerHTML = `

            <div class="empty-state">

                No department data available

            </div>
        `;

        return;
    }


    Object.keys(
        departmentCounts
    ).forEach(
        function (department) {


            const count =
                departmentCounts[
                    department
                ];


            const percentage =
                Math.round(
                    (
                        count /
                        totalEmployees
                    ) * 100
                );


            const item =
                document.createElement(
                    "div"
                );


            item.className =
                "department-overview-item";


            item.innerHTML = `

                <div class="department-details">

                    <div class="department-name-row">

                        <span class="department-name">

                            ${department}

                        </span>


                        <span class="department-percentage">

                            ${percentage}%

                        </span>

                    </div>


                    <div class="department-count">

                        ${count} of ${totalEmployees} employees

                    </div>

                </div>


                <div class="department-bar">

                    <div
                        class="department-bar-fill"

                        style="
                            width: ${percentage}%;
                        "
                    >
                    </div>

                </div>
            `;


            container.appendChild(
                item
            );
        }
    );
}


/* =========================================================
   REPORTS
   ========================================================= */

function renderReports() {


    const totalEmployees =
        employees.length;


    let totalPayroll = 0;


    employees.forEach(
        function (employee) {

            totalPayroll +=
                Number(employee.salary);
        }
    );


    const averageSalary =
        totalEmployees > 0
            ? totalPayroll /
              totalEmployees
            : 0;


    const departmentCounts = {};


    employees.forEach(
        function (employee) {


            if (
                departmentCounts[
                    employee.department
                ]
            ) {

                departmentCounts[
                    employee.department
                ]++;

            } else {

                departmentCounts[
                    employee.department
                ] = 1;
            }
        }
    );


    const departmentCount =
        Object.keys(
            departmentCounts
        ).length;


    const activeCount =
        employees.filter(
            function (employee) {

                return employee.active;
            }
        ).length;


    const activePercentage =
        totalEmployees > 0

            ? Math.round(
                (
                    activeCount /
                    totalEmployees
                ) * 100
            )

            : 0;



    document.querySelector(
        "#reportTotalEmployees"
    ).innerText =
        totalEmployees;


    document.querySelector(
        "#reportTotalPayroll"
    ).innerText =

        "₹" +

        Math.round(
            totalPayroll
        ).toLocaleString("en-IN");


    document.querySelector(
        "#reportAverageSalary"
    ).innerText =

        "₹" +

        Math.round(
            averageSalary
        ).toLocaleString("en-IN");


    document.querySelector(
        "#reportDepartmentCount"
    ).innerText =
        departmentCount;


    document.querySelector(
        "#reportsAverageSalary"
    ).innerText =

        "₹" +

        Math.round(
            averageSalary
        ).toLocaleString("en-IN");


    document.querySelector(
        "#reportsActiveWorkforce"
    ).innerText =

        activePercentage + "%";



    if (totalEmployees > 0) {


        const salaries =
            employees.map(
                function (employee) {

                    return Number(
                        employee.salary
                    );
                }
            );


        document.querySelector(
            "#highestSalary"
        ).innerText =

            "₹" +

            Math.max(
                ...salaries
            ).toLocaleString(
                "en-IN"
            );


        document.querySelector(
            "#lowestSalary"
        ).innerText =

            "₹" +

            Math.min(
                ...salaries
            ).toLocaleString(
                "en-IN"
            );


    } else {


        document.querySelector(
            "#highestSalary"
        ).innerText =
            "₹0";


        document.querySelector(
            "#lowestSalary"
        ).innerText =
            "₹0";
    }



    renderReportsDepartments(
        departmentCounts
    );


    renderReportsEmployees();
}


/* =========================================================
   REPORT DEPARTMENTS
   ========================================================= */

function renderReportsDepartments(
    departmentCounts
) {


    const container =
        document.querySelector(
            "#reportsDepartmentList"
        );


    container.innerHTML = "";


    const totalEmployees =
        employees.length || 1;


    Object.keys(
        departmentCounts
    ).forEach(
        function (department) {


            const count =
                departmentCounts[
                    department
                ];


            const percentage =
                Math.round(
                    (
                        count /
                        totalEmployees
                    ) * 100
                );


            const row =
                document.createElement(
                    "div"
                );


            row.className =
                "report-department-row";


            row.innerHTML = `

                <div class="report-department-top">

                    <strong>
                        ${department}
                    </strong>

                    <span>

                        ${count} employees

                    </span>

                </div>


                <div class="department-bar">

                    <div
                        class="department-bar-fill"

                        style="
                            width: ${percentage}%;
                        "
                    >
                    </div>

                </div>


                <small>

                    ${percentage}% of workforce

                </small>
            `;


            container.appendChild(
                row
            );
        }
    );
}


/* =========================================================
   REPORT EMPLOYEE TABLE
   ========================================================= */

function renderReportsEmployees() {


    const container =
        document.querySelector(
            "#reportsEmployeeTable"
        );


    container.innerHTML = "";


    employees.forEach(
        function (employee) {


            const row =
                document.createElement(
                    "tr"
                );


            row.innerHTML = `

                <td>

                    <strong>

                        ${employee.name}

                    </strong>

                </td>


                <td>

                    ${employee.department}

                </td>


                <td>

                    ${employee.designation}

                </td>


                <td>

                    <span class="report-status">

                        <span class="status-dot"></span>

                        ${
                            employee.active
                                ? "Active"
                                : "Inactive"
                        }

                    </span>

                </td>


                <td>

                    ₹${Number(
                        employee.salary
                    ).toLocaleString(
                        "en-IN"
                    )}

                </td>
            `;


            container.appendChild(
                row
            );
        }
    );
}


/* =========================================================
   VIEW SWITCHER
   ========================================================= */

function showView(
    viewToShow,
    activeNav
) {


    dashboardView.style.display =
        "none";

    employeesView.style.display =
        "none";

    departmentsView.style.display =
        "none";

    reportsView.style.display =
        "none";

    settingsView.style.display =
        "none";


    navLinks.forEach(
        function (link) {

            link.classList.remove(
                "active"
            );
        }
    );


    viewToShow.style.display =
        "block";


    activeNav.classList.add(
        "active"
    );
}


/* =========================================================
   NAV EVENTS
   ========================================================= */

dashboardNav.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        showView(
            dashboardView,
            dashboardNav
        );
    }
);


employeesNav.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        showView(
            employeesView,
            employeesNav
        );
    }
);


departmentsNav.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        showView(
            departmentsView,
            departmentsNav
        );
    }
);


reportsNav.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        renderReports();

        showView(
            reportsView,
            reportsNav
        );
    }
);


settingsNav.addEventListener(
    "click",
    function (event) {

        event.preventDefault();

        showView(
            settingsView,
            settingsNav
        );
    }
);


/* =========================================================
   VIEW ALL
   ========================================================= */

viewAllEmployees.addEventListener(
    "click",
    function () {

        showView(
            employeesView,
            employeesNav
        );
    }
);


/* =========================================================
   START
   ========================================================= */

loadEmployees();