# Rabobank Power of Attorney Assignment

This project enables users to grant other users access to their bank accounts. Account owners can specify whether the granted users have view-only (read) or full management (write) permissions for their payment and savings accounts. Additionally, users can view a list of accounts to which they have been granted access.


---

## What You Can Do With This Project

* **User Registration and Login**: Securely register new users and log in to access the application features.
* **Open Bank Accounts**: Users can open new payment or savings accounts within the system.
* **Create Power of Attorney grants**: One user (the grantor) can give another user (the grantee) permission to access their accounts.
* **Define Permissions**: Assign **read** or **write** access rights on payment or savings accounts.
* **Retrieve Account Access**: Get a list of accounts a user has access to, including their read or write authorizations.

---

## Important Features

* **JWT Authentication**: Secure login mechanism using JSON Web Tokens (JWT) to authenticate and verify user identity.
* **Business Logic**: Clear rules for creating PoA permissions and retrieving authorized accounts.
* **Separation of Concerns**: The application is divided into three modules:
    * **API**: Handles incoming requests.
    * **Domain**: Contains the core business model.
    * **Data**: Manages persistent storage with MongoDB.
* **Validation and Exception Handling**: Robust input validations and clear error responses for issues like duplicate entries or invalid grants.
* **Data Modeling**: MongoDB documents are optimized with indexes for efficient permission queries.
* **Tests**: Unit tests for domain classes and service layers ensure the correctness of the logic.

---

## How to Set Up and Run with Docker

The easiest way to run this entire application is using **Docker**. You won't need to install Java or MongoDB locally.

### Prerequisites

* **Docker**: Make sure Docker is installed and running on your machine. You can download it from the [official Docker website](https://www.docker.com/get-started).

### Steps

1.  **Clone the project repository**:
    ```bash
    git clone <repository-url>
    cd <repository-folder>
    ```

2.  **Build and run the Docker containers**:
    Navigate to the project's root directory in your terminal and run the following command:
    ```bash
    docker-compose up --build
    ```
    This command will:
    * Build the necessary Docker images (for the application and MongoDB).
    * Start the application and MongoDB services.

3.  **Access the API**:
    Once the Docker containers are up and running, the API will be available at:
    `http://localhost:8080`

    You should see logs in your terminal indicating that the application services have started successfully.

---

## Using Postman

A Postman collection is included in the `postman` folder of this project.

1.  **Import the Postman Collection**:
    Import the `Rabobank.postman_collection.json` file into Postman.

2.  **Set Up the Postman Environment**:
    Import the `Development Environment.postman_environment.json` file to quickly set up variables like the base URL.

This allows you to easily test the API endpoints manually, send requests, modify inputs, and verify responses during development or testing.

---

If anything is unclear or you need further help, feel free to ask!