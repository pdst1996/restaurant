# Recursos de la API

La aplicación usa el contexto `/api`. Cada recurso tiene:

- `GET <ruta>`: listado filtrado, sin filtros devuelve todos.
- `POST <ruta>`: creación con su DTO `*Request`; devuelve el DTO `*Response`.
- `GET <ruta>/count`: número de registros que cumplen los mismos filtros.

Las rutas siguen la convención del controlador de órdenes existente.

| Ruta (después de `/api`) | Filtros por query string |
| --- | --- |
| `/floor/floors` | `id`, `name` |
| `/element-type/element-types` | `id`, `type`, `round` |
| `/plan-element/plan-elements` | `id`, `type`, `label`, `floorId` |
| `/table/tables` | `id`, `tableName`, `capacity`, `floorId`, `shape`, `status` |
| `/item/items` | `id`, `sku`, `barcode`, `categoryId`, `available` |
| `/category/categories` | `id`, `categoryKey` |
| `/allergy/allergies` | `id`, `allergyKey` |
| `/item-attribute/item-attributes` | `id`, `attributeKey` |
| `/item-size/item-sizes` | `id`, `sizeKey` |
| `/user/users` | `id`, `email`, `user`, `fullName`, `roleId`, `active` |
| `/role/roles` | `id`, `name`, `active` |
| `/order/orders` | `id`, `orderNumber`, `status`, `tableId`, `waiterId`, `fromDate`, `toDate` |
| `/order-item/order-items` | `id`, `orderId`, `itemId`, `sizeId`, `quantity`, `fromDate`, `toDate` |
| `/payment/payments` | `id`, `orderId`, `method`, `fromDate`, `toDate` |

Los filtros se combinan con AND y los valores se comparan por igualdad.
`fromDate` y `toDate` son límites inclusivos sobre `createdAt`, en formato
ISO-8601, por ejemplo `2026-10-30T12:00:00Z`; se puede enviar un solo límite.
Los booleanos de filtro son opcionales: omitirlos no filtra, y tanto `true`
como `false` se aplican explícitamente.

`barcode` busca un valor textual de primer nivel del mapa `barcodes` usando
la función PostgreSQL `jsonb_contains_value`. El inicializador instala esta
función únicamente al conectarse a PostgreSQL.

Ejemplos:

```http
GET /api/table/tables?capacity=4
GET /api/item/items?available=false&categoryId=2
GET /api/payment/payments/count?orderId=15&method=CARD
POST /api/table/tables
Content-Type: application/json

{"tableName":"Terraza 1","capacity":4,"floorId":1,"x":0,"y":0,"shape":"CIRCULAR"}
```

La creación de usuarios conserva el contrato existente:
`{"email":"...","password":"...","user":"...","fullName":"...","role":{"id":1}}`.
El DTO anidado `RoleReferenceRequest` solo recibe el ID; los campos adicionales
del rol se ignoran. Tanto el registro como el servicio cargan el rol existente
por ID, cifran la contraseña y nunca la incluyen en las respuestas.
Los DTOs de productos y roles reciben
explícitamente sus booleanos `available` y `active`.

Para crear una orden solo son obligatorios `orderNumber` y `waiterId`:

```http
POST /api/order/orders
Content-Type: application/json

{"orderNumber":"SEED-ORD-232344","waiterId":1}
```

Si se omite `togo` o se envía `null`, se usa `false`; `true` y `false`
explícitos se conservan. Sin `status` ni `total`, la orden inicia en `DRAFT`
con total cero. El mesero debe existir y el número de orden debe ser único.

Los errores MVC (incluido `MALFORMED_JSON`) y las excepciones simples
`ErrorResponseException` incluyen el
`stackTrace` de la excepción original para diagnóstico. Los errores de
autenticación no incluyen trazas. Las trazas contienen detalles internos;
no se recomienda exponerlas públicamente en producción.

Las referencias inexistentes devuelven 404 y los campos únicos duplicados 409.
Los cuerpos y filtros inválidos devuelven 400. Los servicios convierten las
relaciones a DTO dentro de una transacción para no depender de una sesión
Hibernate abierta en el controlador.

Crear partidas o pagos registra sus datos y relaciones; no recalcula el total
de la orden ni modifica automáticamente su estado.

Las partidas aceptan `attributeIds` opcional, con IDs positivos de atributos
existentes. Se eliminan duplicados; una referencia inexistente devuelve 404.
Su respuesta incluye el producto en `item`, el tamaño en `size` y los atributos
como objetos DTO en `attributes`, sin entidades ni referencias circulares.
Los productos guardan y devuelven `img` (hasta 2048 caracteres).

`PUT /order/orders/{id}` devuelve 404 si la orden no existe. Si se omiten
`status` o `total`, conserva sus valores actuales; los demás campos siguen el
contrato de `OrderRequest`.

## Plano del restaurante

Los pisos admiten `name`, `width`, `height` y `sort_order`; al omitir dimensiones
u orden se usan 1200, 800 y 0. Las dimensiones deben ser positivas.
Las mesas requieren un `floorId` existente. Sus coordenadas admiten cero; por
defecto tienen ancho 100, alto 80, rotación 0, forma `RECTANGULAR` y estado `FREE`.
`shape` acepta `RECTANGULAR` o `CIRCULAR`; `status` permite `FREE`, `OCCUPIED` y
`OUT_OF_SERVICE`. La respuesta de mesa incluye el piso como DTO en `floor`.

`ElementTypeRequest` recibe `type`, `label` (mapa de traducciones), `width`,
`height`, `round`, `fill` y `stroke`. `type` es único. Para crear un elemento se
envían `floorId`, el código `type` del catálogo, `x`, `y`, `width`, `height` y,
opcionalmente, `rotation` y `label`. Se valida el piso y se resuelve el tipo a
una relación persistida; la respuesta incluye `elementType` como DTO y conserva
`type` como código de consulta.

`PUT /floor/floors/floor-plan` guarda el plano de forma transaccional:

```json
{
  "tables": [
    {"id": 5, "table": {"tableName": "Terraza 1", "capacity": 4, "floorId": 1, "x": 50, "y": 0}}
  ],
  "elements": [
    {"element": {"floorId": 1, "type": "wall", "x": 0, "y": 0, "width": 200, "height": 12}}
  ],
  "deletedElementIds": [8]
}
```

Un `id` omitido o nulo crea una entidad; un `id` existente la actualiza.
Las listas omitidas o nulas se consideran vacías, sin borrar otros registros.
Una referencia inexistente devuelve 404; guardar dos veces el mismo ID o
guardar y eliminar el mismo elemento devuelve 409. Cualquier fallo revierte
todas las eliminaciones y modificaciones. La respuesta es
`ApiResponse<LayoutResponse>`, con las mesas y elementos guardados en `data`.
Los DTOs de entrada del plano no reutilizan los DTOs de respuesta.

## Persistencia y pruebas

El catálogo de atributos usa `item_attributes`. Su relación muchos-a-muchos
con productos usa `item_attribute_links(item_id, attribute_id)`, evitando que
el catálogo y la tabla de relación compartan el mismo nombre. Hibernate crea
la tabla de relación con `ddl-auto=update`. Si una base anterior ya guardó
enlaces en `item_attributes`, es necesario migrar esos enlaces a la nueva tabla
antes de utilizarla; este cambio de código no modifica los datos existentes.

`./mvnw clean test` ejecuta las pruebas con H2 en memoria. La clase
`ResourceControllersIntegrationTests` prueba los controladores con servicios
y repositorios reales, e incluye el filtro JSONB cuando se ejecuta contra
una base PostgreSQL de pruebas. No usar una base con datos reales con
`ddl-auto=create-drop`.
