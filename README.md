# Sistema de Gestión Hospitalaria - María Auxiliadora 🏥

Breve descripción de tu proyecto. (Ejemplo: Sistema web desarrollado para optimizar la gestión de citas, 
pacientes y personal médico del Hospital María Auxiliadora).

## Tecnologías Utilizadas
* **Frontend:** Angular 
* **Backend:** Java, Spring Boot
* **Base de Datos:** MySQL (u Oracle/PostgreSQL, la que estés usando)

## Estructura del Proyecto
El repositorio está dividido en tres partes principales:
* `hospital-frontend/`: Interfaz de usuario.
* `hospital-backend/`: API REST y lógica de negocio.
* `Query_Hospital_MariaAuxiliadora.sql`: Script para inicializar la base de datos.

## 🚀 Cómo ejecutar el proyecto localmente

### 1. Base de Datos
1. Ejecuta el script `Query_Hospital_MariaAuxiliadora` en tu gestor de base de datos.
2. Configura las credenciales en el archivo `application.properties` del backend.

### 2. Backend (Spring Boot)
1. Abre la carpeta `hospital-backend` en tu IDE.
2. Instala las dependencias y ejecuta la clase principal.
3. La API estará corriendo en `http://localhost:8080`.

### 3. Frontend (Angular)
1. Abre una terminal en la carpeta `hospital-frontend`.
2. Ejecuta `npm install` para instalar las dependencias.
3. Ejecuta `ng serve` para levantar el servidor de desarrollo.
4. Abre tu navegador en `http://localhost:4200`.