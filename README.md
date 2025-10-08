# 🗓️ Students' Online Scheduler (SOS)

**Students' Online Scheduler (SOS)** is a modular JavaFX application for collaboratively scheduling appointments.  
It allows users to **register**, **create surveys with time options**, **invite participants**, and **vote on preferred dates**.  

This project was developed as part of the SWE4 course at FH Salzburg during SS2025 and gradually extended from a single-user prototype to a distributed client-server system with a MySQL backend.

---

## Project Overview

The project is built using **Java 21**, **JavaFX**, and **Java RMI** with a **MySQL** database.  
The core architecture follows the **Model-View-Controller (MVC)** pattern, ensuring a clean separation between UI, logic, and data.

-  Modular UI with FXML  
-  Singleton-based service management (e.g., SceneManager, Repository, Authentication)  
-  Flexible data storage layer (in-memory → RMI → MySQL)  
-  Distributed client-server architecture using RMI  
-  Simple development setup using Docker and PowerShell scripts

---

##  Architecture

### 1. Model
Represents the core domain logic:
- `User` – stores user credentials and IDs.
- `AppointmentSurvey` – represents an appointment poll with options and participants.
- `SurveyOption` – holds a single time option and the users who voted for it.

### 2. View
User interface built with **JavaFX FXML**:
- `LoginView.fxml` – user login  
- `DashboardView.fxml` – overview of own and invited surveys  
- `SurveyView.fxml` – survey detail and voting  
- `CreateSurveyView.fxml` – creating new surveys  
- `InviteUserPopupView.fxml` – modal popup for inviting participants

### 3. Controller
Handles all UI interactions:
- `LoginController` – login flow  
- `DashboardController` – loads and displays surveys  
- `SurveyController` – voting and survey management  
- `CreateSurveyController` – creating new surveys  
- `InviteUserPopupController` – handling invitations

### 4. Data Layer
The data access is abstracted through the `Repository` interface with multiple implementations:
- `FakeRepository` – in-memory for development & testing  
- `DBRepository` – JDBC MySQL-based implementation  
- All repository methods are fully isolated from the UI, making the system easily extensible.

### 5. Navigation – `SceneManager`
- Singleton class for UI navigation and scene switching.  
- Provides `switchTo(...)`, `showPopup(...)`, and related methods.  
- Centralizes scene handling for the entire application.

### 6. Authentication
- `AuthenticationService` provides global access to the currently logged-in user.  
- Used by controllers to restrict access to specific actions.

---

##  Client–Server Extension (RMI)

From version 2 onward, SOS is extended to a **distributed architecture**:
- The **server** hosts the data and business logic via RMI.
- **Clients** run the JavaFX UI and communicate with the server.
- `ClientCallback` interface allows **real-time updates** — e.g., when new surveys or votes are added.

---

##  Database Integration

From version 3 onward, all data is persisted in a **MySQL** database:
- `DBRepository` handles all queries using **JDBC** and **PreparedStatements**.
- Uses **transaction management** for complex operations.
- Schema is fully defined and created using scripts in the `scripts` directory.
- `ON DELETE CASCADE` ensures data integrity on survey deletion.

---

##  Development Setup

### Prerequisites
- Java 21  
- Maven  
- Docker  
- PowerShell (Windows)

## To start the project

In case you want to start without server and DB. (Client-Only, No data persistence)
### 1. Go to `/src/main/java/swe4/sos/gui/GuiDemo.java` and start the class here.

---

Normal startup with server and DB.
### 1. Start the Docker Desktop
### 2. Go to `/scripts` folder and start the scripts in the following order:
- `start-mysql-server.ps1` - starts the mysql container
- `create-sos-schema.ps1` - creates the schema in the existing mysql container
### 3. Go to `/src/main/java/swe4/sos/server/server/Server.java` and start the class here.
### 4. Go to `/src/main/java/swe4/sos/server/client/Client.java` and start the class here (Both Server and Client how to be running parallely).
    

