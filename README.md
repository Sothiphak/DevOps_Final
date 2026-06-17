# Aura ID Card Suite - Premium ID Management System

A robust, enterprise-grade Spring Boot application designed to manage, preview, and generate student and employee ID cards dynamically with barcodes, verification QR codes, and custom orientation templates.

---

## 🚀 Key Features

*   **Full Profile CRUD**: Comprehensive administration dashboard to create, read, update, and delete profiles (Supporting Student, Employee, and general User types).
*   **Secure Photo Uploads**: Strict validation supporting JPEG/PNG formats up to 5MB, stored safely in a dedicated directory.
*   **Template Customization Engine**: Select color schemes, layouts (Vertical/Horizontal), organization names, and taglines dynamically.
*   **Live Preview Interface**: Real-time rendering of card details, barcodes, and verification QR codes as details are input in the form.
*   **Automatic Registration ID Generator**: Sequential custom ID generation (`YEAR-DEPT-RANDOM_NUM`) checks db existence to prevent duplicates.
*   **iText PDF Exports**: Generate single and batch multi-page PDF cards directly from the system canvas, with layered images rendering on top of color blocks.
*   **Verification Portal**: Fully integrated QR codes redirecting to a secure verification portal (`/profiles/verify/{uuid}`) with validity seal status badges.
*   **Dual Barcode Support**: Dynamic generation of both `Code-128` and `EAN-13` formats (with automatic EAN check-digit fallback calculations).

---

## 🛠️ Technology Stack

*   **Backend Framework**: Spring Boot (v3.5.15), Java 23, Spring Data JPA
*   **Frontend Template Engine**: Thymeleaf, HTML5, Modern Vanilla CSS (Glassmorphism design)
*   **Database**: MariaDB / MySQL (Production), H2 (In-memory testing)
*   **Libraries**:
    *   **iText (5.5.13.3)**: Dynamic PDF Canvas Layouts
    *   **Google ZXing (3.5.3)**: Real-time Barcode & QR Code Generation

---

## 💻 Local Setup & Execution

### 1. Database Configuration
Make sure MariaDB or MySQL is running on port `3306`.
Create the database and a user matching the defaults (or override using environment variables):

```sql
CREATE DATABASE IF NOT EXISTS idcard_db;
CREATE USER 'idcard_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON idcard_db.* TO 'idcard_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Run the Application
The port is configured to `8082` by default to avoid conflict with running local Jenkins servers (`8080`).

Run the following command from the project root:
```bash
./mvnw spring-boot:run
```

Once started, access the dashboard in your web browser:
👉 **[http://localhost:8082](http://localhost:8082)**

---

## 🧪 Running Tests
Unit and integration tests are isolated using H2 database:
```bash
./mvnw test
```
