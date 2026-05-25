# 🧾 Sistema de Facturación Electrónica - SuperMarket

Sistema desarrollado en Java con POO, Vaadin, PostgreSQL/JDBC, SLF4J/Logback y Maven.

---

## 🏗️ Arquitectura del Proyecto

```
facturacion-electronica/
├── pom.xml
└── src/main/java/com/facturacion/
    ├── FacturacionApp.java              ← Punto de entrada Spring Boot
    ├── config/
    │   ├── DatabaseConnection.java      ← Singleton HikariCP (pool de conexiones)
    │   └── SecurityConfig.java         ← Spring Security + Vaadin
    ├── model/                           ← Entidades POO
    │   ├── Turno.java
    │   ├── Empleado.java
    │   ├── Cliente.java
    │   ├── Producto.java
    │   ├── DetalleFactura.java
    │   └── Factura.java
    ├── dao/                             ← Capa de acceso a datos (JDBC puro)
    │   ├── GenericDAO.java              ← Interfaz genérica CRUD
    │   ├── EmpleadoDAO.java
    │   ├── ClienteDAO.java
    │   ├── ProductoDAO.java
    │   └── FacturaDAO.java             ← Transacciones ACID
    ├── service/                         ← Lógica de negocio
    │   ├── AuthService.java             ← Autenticación BCrypt + sesión
    │   └── FacturacionService.java      ← Orquestación de facturación
    ├── ui/                              ← Capa de presentación Vaadin
    │   ├── MainLayout.java              ← Sidebar + navegación por rol
    │   └── views/
    │       ├── LoginView.java           ← Pantalla de login
    │       ├── FacturacionView.java     ← Nueva factura (vista principal)
    │       └── AllViews.java            ← Historial, Productos, Clientes, Usuarios, Reportes
    └── util/
        └── PdfGeneratorUtil.java        ← Generación de PDF con iText
```

---

## ⚙️ Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- IntelliJ IDEA (recomendado)

---

## 🚀 Configuración y Ejecución

### 1. Crear la Base de Datos

```bash
psql -U postgres
\i src/main/resources/schema.sql
```

Esto crea la BD `facturacion_db` con todas las tablas y datos iniciales.

### Migraciones con Flyway
- Se recomienda gestionar las migraciones de BD con Flyway (integrado vía Spring Boot).
- Las migraciones deben ubicarse en `src/main/resources/db/migration` y se ejecutarán automáticamente al iniciar la aplicación.
- Ejemplo: `V1__init_schema.sql` contiene la creación inicial de tablas y datos de ejemplo (equivalente al contenido de `schema.sql`).

### 2. Configurar Credenciales

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/facturacion_db
spring.datasource.username=postgres
spring.datasource.password=TU_PASSWORD_AQUI
```

### 3. Compilar y Ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

Abrir: http://localhost:8080

---

## 🔑 Credenciales por Defecto

| Usuario | Contraseña | Rol           |
|---------|-----------|---------------|
| admin   | Admin123! | ADMINISTRADOR |

---

## 📦 Módulos del Sistema

| Módulo              | Ruta Vaadin  | Roles         |
|---------------------|--------------|---------------|
| Nueva Factura       | `/`          | Todos         |
| Historial Facturas  | `/historial` | Todos         |
| Gestión Productos   | `/productos` | Todos         |
| Gestión Clientes    | `/clientes`  | Todos         |
| Gestión Usuarios    | `/usuarios`  | Administrador |
| Reportes            | `/reportes`  | Administrador |

---

## 🛠️ Tecnologías

| Tecnología         | Uso                                      |
|--------------------|------------------------------------------|
| **Vaadin 24**      | Framework UI (capa de presentación)      |
| **Java 17 (POO)**  | Backend con entidades, servicios y DAOs  |
| **PostgreSQL**     | Base de datos relacional                 |
| **JDBC + HikariCP**| Acceso a datos y pool de conexiones      |
| **SLF4J + Logback**| Sistema de logs (consola + archivos)     |
| **Maven**          | Gestión de dependencias (`pom.xml`)      |
| **BCrypt (jBCrypt)**| Hash seguro de contraseñas              |
| **iText 5**        | Generación de facturas en PDF            |

---

## 📋 Patrones de Diseño Aplicados

- **Singleton**: `DatabaseConnection`, `AuthService`
- **DAO Pattern**: `GenericDAO<T,ID>` + implementaciones específicas
- **Layered Architecture**: Presentación → Negocio → Datos
- **Template Method**: Herencia en modelos (`Persona → Empleado/Cliente`)

---

## 📁 Logs

Los logs se generan en:
- `logs/facturacion.log` — Log general (INFO+)
- `logs/errors.log`      — Solo errores (ERROR)
- Consola               — Desarrollo (INFO+)

Rotación automática diaria, conserva 30 días.
