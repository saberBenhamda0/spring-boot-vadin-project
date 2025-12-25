# Event Booking Management System

## Description
A comprehensive Event Booking Management System built with Spring Boot and Vaadin. This application allows users to browse events, make reservations, and allows organizers to manage their events. The system includes role-based access control for Admins, Organizers, and Clients.

## Technology Stack
- **Backend:** Java 17, Spring Boot 3.5.8
- **Frontend:** Vaadin 24.9.6
- **Database:** H2 Database (In-memory)
- **Security:** Spring Security
- **Persistence:** Spring Data JPA

## Features
- **Event Management:** Create, update, and manage events (Concerts, Theatre, Conferences, Sports, etc.)
- **Reservation System:** Users can book tickets for events.
- **Role-Based Access:** Distinct features for distinct roles (Admin, Organizer, Client).
- **Dashboard:** Visual overview of events and reservations.


## Architecture de l'application (Diagramme de composants)

```mermaid
graph TD
    User((Utilisateur))
    Browser[Navigateur Web]
    
    subgraph "Application Spring Boot"
        subgraph "Présentation (Vaadin)"
            Auth[Security Config]
            Views[Vues (Views)]
        end
        
        subgraph "Logique Métier"
            Services[Services]
        end
        
        subgraph "Accès aux Données"
            Repos[Repositories]
        end
        
        subgraph "Modèle"
            Entities[Entités JPA]
        end
    end
    
    subgraph "Persistance"
        DB[(Base de données H2)]
    end

    User -->|HTTPS| Browser
    Browser <-->|Vaadin Flow| Views
    
    Views --> Services
    Views -.->|Protection| Auth
    
    Services --> Repos
    Services -.->|Manipule| Entities
    
    Repos --> DB
    Repos -.->|Mappe| Entities
```

## Getting Started

### Prerequisites
- Java 17+
- Maven

### Running the Application
1. Clone the repository.
2. Navigate to the project directory.
3. Run the application using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Access the application at `http://localhost:8080`.

## Default Login Credentials

The application is pre-loaded with the following sample users:

### Admin
- **Email:** `admin@event.ma`
- **Password:** `admin123`
- **Role:** `ADMIN`

### Organizers
- **Organizer 1:**
  - **Email:** `org1@event.ma`
  - **Password:** `organizer123`
- **Organizer 2:**
  - **Email:** `org2@event.ma`
  - **Password:** `organizer123`

### Clients
- **Client 1:**
  - **Email:** `client1@event.ma`
  - **Password:** `client123`
- **Client 2:**
  - **Email:** `client2@event.ma`
  - **Password:** `client123`
- **Test User:**
  - **Email:** `test@gmail.com`
  - **Password:** `testtest`
