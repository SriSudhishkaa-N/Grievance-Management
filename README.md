# Government Grievance Management and Priority Resolution System

## Project Overview

The Government Grievance Management and Priority Resolution System is a web-based application that allows citizens to submit and track public service complaints.

It helps administrators and government department officers manage complaints through automatic priority calculation, department assignment and duplicate complaint detection.

## Technologies Used

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- MySQL
- HTML
- CSS
- JavaScript
- Maven
- Git and GitHub

## Main Features

1. Citizen Registration
2. Duplicate Registration Prevention
3. Online Complaint Submission
4. Duplicate Complaint Detection
5. Automatic Priority Calculation
6. Automatic Department Assignment
7. Complaint Tracking
8. Admin Dashboard
9. Department Officer Dashboards
10. Supporting Complaint Reports
11. Complaint Status Updates
12. PDF Report Generation

## Departments

- Water Supply Department
- Public Works Department
- Electricity Department
- Sanitation Department

## Priority Calculation

| People Affected | Priority |
|---|---|
| 1–10 | LOW |
| 11–50 | MEDIUM |
| Above 50 | HIGH |

## Database

MySQL is used to store application information.

Main database tables:

- citizens
- complaints
- complaint_reports

## How to Run the Project

### Prerequisites

- Java Development Kit
- MySQL Server
- Visual Studio Code

### Execution Steps

1. Open the project in Visual Studio Code.
2. Start the MySQL service.
3. Configure the MySQL connection in `application.properties`.
4. Open the project root directory in the terminal.
5. Run:

```powershell
.\mvnw.cmd spring-boot:run
```

6. Open your browser and visit:

http://localhost:8080/index.html

## GitHub Repository

https://github.com/SriSudhishkaa-N/Grievance-Management

## Conclusion

This project provides an organized platform for citizen grievance management. It simplifies complaint submission and tracking while helping administrators and department officers manage public complaints efficiently.