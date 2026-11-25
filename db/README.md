# Corrección de tablas PostgreSQL

Las migraciones numeradas para permisos, historial, caja y catálogo se
documentan en [CAMBIOS.md](../docs/CAMBIOS.md). Los scripts descritos aquí
solo aplican a las variantes históricas del esquema.

Los IDs de `users` y `roles` son `bigint` con generación `IDENTITY`.
`orders.waiter_id` debe usar el mismo tipo. `orders.order_number` se almacena
como `varchar(255)` y se usa como `String` en el modelo, DTOs, filtros y
repositorio. El script histórico convierte `order_items.size` de ordinal a texto;
el modelo actual usa un catálogo `item_size` y una FK `order_items.item_size`.
Las alergias y atributos actuales son relaciones muchos-a-muchos, no arreglos.
`ddl-auto=update` no migra los valores de arreglos antiguos a estas relaciones:
hay que resolverlos contra los catálogos y copiar los enlaces antes de utilizar
una base creada con ese esquema anterior. Conservar una copia de seguridad.

Si la base ya contiene el esquema parcial anterior, ejecutar antes de arrancar:

```sh
psql -h localhost -U postgres -d restaurant -v ON_ERROR_STOP=1 -f db/fix-table-types.sql
```

El script no elimina tablas ni registros, es transaccional y se puede repetir.
Los folios de texto existentes, como `111111`, `000001` y `ORD-2026-001`,
se conservan sin cambios. Si `order_number` ya es `uuid`, el script lo
convierte a `varchar(255)` conservando la representación textual del valor.
No convierte texto a UUID ni genera folios automáticamente: cada orden nueva
debe recibir un `orderNumber` no vacío de hasta 255 caracteres, único en la base.

Después, iniciar la aplicación con Java 17 o superior: `ddl-auto=update` crea
las tablas y relaciones que faltan, pero no sustituye una migración de datos.

## Pisos, mesas y elementos

`Table.floor` es obligatorio. Antes de aplicar el modelo a una base con mesas
existentes sin `floor_id`, asignar a cada mesa su piso real; no se elige un piso
automáticamente ni se trasladan mesas existentes desde los seeders.

`shape` y `table_status` se guardan como texto. Si ya existen como ordinales,
usar `db/floor-plan-migration.sql` antes de arrancar. El mismo script enlaza el
antiguo `element.type` con `element.element_type_id` usando `element_type.type`.
El catálogo de tipos debe existir y contener los códigos de todos los elementos.
El script se puede repetir, conserva la columna antigua y revierte toda la
operación si encuentra códigos u ordinales desconocidos. Revisarlo y respaldar
la base antes de ejecutarlo; no se ejecuta automáticamente con la aplicación.

```sh
psql -h localhost -U postgres -d restaurant -v ON_ERROR_STOP=1 -f db/floor-plan-migration.sql
```

Con `app.seed.enabled=true` se crean, en orden, roles, usuarios, pisos y tipos
de elemento, elementos del plano, mesas, catálogos y productos, alergias y
órdenes con partidas y pagos. Los registros existentes se conservan y las
ejecuciones posteriores no agregan duplicados. Los usuarios y roles nuevos
se crean activos; las contraseñas de prueba se cifran con BCrypt.

Las pruebas de persistencia usan el perfil `test` y una base H2 en memoria,
independiente de PostgreSQL.
