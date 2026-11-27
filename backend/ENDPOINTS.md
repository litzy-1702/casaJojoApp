# CasaJojo — Endpoints de los microservicios

Contrato entre el backend (Spring Boot + Kotlin) y la app Android.
Ambos servicios usan H2 en modo archivo (`./data/`), así que los datos se mantienen al reiniciar.

| Servicio | Puerto | URL base (PC) | URL base (emulador Android) | Consola H2 |
|---|---|---|---|---|
| menu-service | 8081 | `http://localhost:8081` | `http://10.0.2.2:8081` | `/h2-console` · JDBC `jdbc:h2:file:./data/menudb` · usuario `sa` |
| pedido-service | 8082 | `http://localhost:8082` | `http://10.0.2.2:8082` | `/h2-console` · JDBC `jdbc:h2:file:./data/pedidodb` · usuario `sa` |

> Desde el emulador, `localhost` apunta al propio emulador: hay que usar `10.0.2.2`.

---

## menu-service — `/api/menu`

| Método | Ruta | Uso en la app | Cuerpo | Respuesta |
|---|---|---|---|---|
| GET | `/api/menu` | Listar todos los platos | — | 200 · lista ordenada por fecha y nombre |
| GET | `/api/menu?fecha=AAAA-MM-DD` | Menú del día | — | 200 · lista ordenada por nombre |
| GET | `/api/menu/{id}` | Ver un plato | — | 200 · plato / 404 |
| POST | `/api/menu` | Agregar plato (Admin) | `MenuItemRequest` | 201 · plato creado / 400 |
| PUT | `/api/menu/{id}` | Editar plato (Admin) | `MenuItemRequest` | 200 · plato / 400 / 404 |
| PATCH | `/api/menu/{id}/disponibilidad` | Agotar o reactivar plato | `{"disponible": false}` | 200 · plato / 400 / 404 |
| DELETE | `/api/menu/{id}` | Eliminar plato (Admin) | — | 204 / 404 |

**MenuItemRequest**

```json
{
  "nombre": "Pollo asado",
  "descripcion": "Incluye mix de ensaladas. Selecciona uno o dos agregados.",
  "precio": 4800,
  "disponible": true,
  "imagenUri": null,
  "fechaMenu": "2026-11-27"
}
```

Reglas: `nombre` y `descripcion` obligatorios; `precio` mayor que 0; `fechaMenu` con formato `AAAA-MM-DD`; `disponible` es `true` por defecto; `imagenUri` es opcional.

**Plato (respuesta)**: los mismos campos más `id`.

**Datos iniciales**: al arrancar, menu-service agrega para la fecha de hoy los 6 platos base que falten (Guatitas a la jardinera, Cerdo mongoliano, Filete de pollo y Tortilla de porotos verdes a $4.600; Pollo asado y Carne al jugo a $4.800). No duplica los que ya existen.

---

## pedido-service — `/api/pedidos`

| Método | Ruta | Uso en la app | Cuerpo | Respuesta |
|---|---|---|---|---|
| GET | `/api/pedidos` | Todos los pedidos (Caja / historial) | — | 200 · lista, más recientes primero |
| GET | `/api/pedidos?estado=RECIBIDO` | Pedidos por estado (Cocina) | — | 200 · lista, más antiguos primero |
| GET | `/api/pedidos/{id}` | Ver un pedido | — | 200 · pedido / 404 |
| POST | `/api/pedidos` | Confirmar pedido (Cliente) | `PedidoRequest` | 201 · pedido creado / 400 |
| PATCH | `/api/pedidos/{id}/estado` | Cambiar estado (Caja / Cocina) | `{"estado": "EN_PREPARACION"}` | 200 · pedido / 400 / 404 |
| PATCH | `/api/pedidos/{id}/pago` | Marcar pagado o pendiente (Caja) | `{"pagado": true}` | 200 · pedido / 400 / 404 |
| DELETE | `/api/pedidos/{id}` | Eliminar pedido | — | 204 / 404 |

**PedidoRequest**

```json
{
  "clienteNombre": "María González",
  "clienteTelefono": "912345678",
  "detalleItems": "1x Pollo asado | Agregados: Arroz normal, Puré natural | Ensalada incluida: Mix de ensaladas",
  "restricciones": "Sin sal",
  "horaComprometida": "13:30",
  "total": 4800,
  "metodoPago": "Efectivo"
}
```

Reglas: `clienteNombre`, `detalleItems` y `horaComprometida` (`"HH:mm"` o `"Lo antes posible"`) obligatorios; `clienteTelefono` solo dígitos (máximo 12, puede ir vacío); `restricciones` máximo 150 caracteres; `total` mayor que 0; `metodoPago` es `"Efectivo"` por defecto; `horaCreacion` (milisegundos) es opcional y, si no se envía, la asigna el servidor. El servidor siempre crea el pedido con `estado = RECIBIDO` y `pagado = false`.

**Pedido (respuesta)**

```json
{
  "id": 1,
  "clienteNombre": "María González",
  "clienteTelefono": "912345678",
  "detalleItems": "1x Pollo asado | Agregados: Arroz normal, Puré natural | Ensalada incluida: Mix de ensaladas",
  "restricciones": "Sin sal",
  "horaCreacion": 1795795200000,
  "horaComprometida": "13:30",
  "total": 4800,
  "estado": "RECIBIDO",
  "pagado": false,
  "metodoPago": "Efectivo"
}
```

**Estados** (mismos nombres que `EstadoPedido` de la app): `RECIBIDO`, `EN_PREPARACION`, `LISTO`, `ENTREGADO`, `CANCELADO`.

---

## Errores (ambos servicios)

Todas las respuestas de error tienen el mismo formato:

```json
{
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Los datos enviados no son válidos",
  "errores": {
    "clienteNombre": "El nombre del cliente es obligatorio",
    "total": "El total debe ser mayor que cero"
  }
}
```

| Código | Cuándo |
|---|---|
| 400 | Campos inválidos (`errores` indica cada campo), JSON mal formado, estado inexistente o parámetro con tipo incorrecto |
| 404 | El id no existe (por ejemplo, `"mensaje": "No existe el pedido con id 99"`) |

---

## Pruebas con Postman

Colección: `backend/postman/CasaJojo-backend.postman_collection.json` (carpetas `menu-service` y `pedido-service`).
Con los dos servicios corriendo, se ejecuta completa con **Run collection**. La variable `hoy` se calcula sola antes de cada petición.
