# CasaJojo App

Aplicación móvil Android nativa desarrollada en **Kotlin** y **Jetpack Compose** junto a un ecosistema de **microservicios Spring Boot**, diseñada para digitalizar y optimizar la gestión operativa del restaurante **La Casa Jo-Jo** (administración de menús diarios, toma de pedidos de clientes, cobros en caja y despacho en cocina).

---
## Integrantes

* **Benjamín Figueroa** 
* **Litzy Zuñiga**
* **Benjamín Brown**
---
## 1. Descripción General y Roles del Sistema

El sistema resuelve el flujo completo de atención del restaurante bajo una arquitectura **Offline-First** (persistencia local en SQLite/Room sincronizada con APIs REST):

* **Cliente:** Ingresa sus datos básicos (nombre y teléfono), visualiza el menú disponible del día, personaliza cada plato eligiendo entre 1 y 2 agregados (con ensalada mixta incluida), gestiona su carrito, define hora de retiro ("Lo antes posible" o programada) con observaciones alimentarias y hace seguimiento en tiempo real del estado de su pedido.
* **Administrador:** Accede a un Dashboard con métricas diarias (pedidos recibidos, pendientes, en preparación, listos y ventas cobradas), administra el CRUD de platos del menú (incluyendo carga masiva del menú base y fotografías), consulta historial filtrado de pedidos y gestiona cuentas de trabajadores.
* **Caja:** Supervisa las comandas en tiempo real, detecta pedidos atrasados, valida y cambia estados de pago (`PAGADO` / `PENDIENTE PAGO`) y actualiza el ciclo de vida de cada pedido.
* **Cocina:** Visualiza en cuadrícula las comandas activas (`RECIBIDO` y `EN_PREPARACION`) con alertas visuales de atraso y restricciones alimentarias destacadas para preparar y marcar pedidos como `LISTO`[cite: 12].

---

## 2. Arquitectura y Stack Tecnológico

### Frontend Móvil (Android)
* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose + Material Design 3
* **Arquitectura:** MVVM (Model-View-ViewModel) con flujo unidireccional de datos (`StateFlow` y Kotlin Coroutines)
* **Navegación:** Jetpack Navigation Compose con control de rutas por rol y bloqueo de retroceso en sesiones activas
* **Persistencia Local:** Room Database (`casajojo_database.db`, versión 2 con migración `MIGRATION_1_2`)
* **Consumo de APIs:** Retrofit 2 + OkHttp Logging Interceptor + Gson
* **Carga de Imágenes:** Coil (`AsyncImage`) + Android Photo Picker (`PickVisualMedia`)
* **Notificaciones:** `NotificationCompat` y canales de notificación (`OrderNotificationManager`) para alertas de cambio de estado en comandas
* **Seguridad:** Hashing de contraseñas con **PBKDF2WithHmacSHA256** (120.000 iteraciones + Salt aleatorio de 16 bytes) y validación de RUT chileno con dígito verificador[cite: 12]

### Backend y Servicios Externos
* **Microservicio de Menú (`backend/menu-service`):** Spring Boot (Kotlin) en puerto `8081` (`/api/menu`)
* **Microservicio de Pedidos (`backend/pedido-service`):** Spring Boot (Kotlin) en puerto `8082` (`/api/pedidos`)
* **API Externa:** Integración con **TheMealDB** (`https://www.themealdb.com/api/json/v1/1/search.php`) para sugerencias culinarias[cite: 12]

---

## 3. Estructura del Proyecto

```text
casaJojoApp/
├── app/src/main/java/com/example/casajojo/
│   ├── core/notificaciones/     # Gestión de notificaciones locales de comandas
│   ├── data/
│   │   ├── external/            # Cliente Retrofit y repositorio para TheMealDB
│   │   ├── local/               # AppDatabase, DAOs (Menu, Pedido, Usuario) y Converters
│   │   ├── model/               # Entidades Room y reglas del menú base (MenuCasaJojo)
│   │   ├── network/             # Servicios Retrofit, DTOs y mapeadores (10.0.2.2)
│   │   └── repository/          # Repositorios híbridos (Room + API REST)
│   ├── feature/
│   │   ├── admin/               # Dashboard, CRUD de menú, filtro de pedidos y validadores
│   │   ├── auth/                # Login, cambio obligatorio de clave, gestión de usuarios y hashing
│   │   ├── caja/                # Pantalla de control de caja y cobros
│   │   ├── client/              # Menú cliente, personalización de agregados, carrito, checkout y estado
│   │   ├── cocina/              # Pantalla de comandas para cocina
│   │   └── operations/          # Estados de pedido y OperationsViewModel
│   ├── navigation/              # Grafo de navegación y control de acceso por rol
│   └── ui/theme/                # Paleta de colores, tipografía y tema Material 3
├── app/src/test/                # Pruebas unitarias (JUnit4 + Coroutines Test)
└── backend/
    ├── menu-service/            # Microservicio Spring Boot para gestión de menú (Puerto 8081)
    ├── pedido-service/          # Microservicio Spring Boot para gestión de pedidos (Puerto 8082)
    └── postman/                 # Colección Postman para pruebas de endpoints
```[cite: 12]

---

## 4. Requisitos Previos e Instalación

1. **Android Studio** (Ladybug o superior) con **JDK 21** y Android SDK configurado.
2. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/litzy-1702/casaJojoApp.git](https://github.com/litzy-1702/casaJojoApp.git)
   cd casaJojoApp

## Evidencia de Compilación (APK Firmado y Keystore .jks)
```
[Evidencia APK firmado y archivo .jks](docs/apk-firmado.png)

## 5. Pasos para Ejecutar el Proyecto

### A. Ejecutar los Microservicios (Backend)
1. Abrir dos terminales en la carpeta raíz del proyecto.
2. En la primera terminal, iniciar `menu-service` (puerto `8081`):
   ```bash
   cd backend/menu-service
   ./mvnw spring-boot:run
3. En la segunda terminal, iniciar `pedido-service` (puerto `8082`)
    ```bash
   cd backend/pedido-service
   ./mvnw spring-boot:run