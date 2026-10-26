# Corrección de tablas PostgreSQL

Los IDs de `users` y `roles` son `bigint` con generación `IDENTITY`.
`orders.waiter_id` debe usar el mismo tipo y `order_items.size` guarda el
nombre de `ItemSize`, no su posición en el enum. Las listas de alergias y
atributos se mantienen como arreglos nativos para conservar los datos existentes.

Si la base ya contiene el esquema parcial anterior, ejecutar antes de arrancar:

```sh
psql -h localhost -U postgres -d restaurant -v ON_ERROR_STOP=1 -f db/fix-table-types.sql
```

El script no elimina tablas ni registros, es transaccional y se puede repetir.
Después, iniciar la aplicación con Java 17 o superior: `ddl-auto=update` crea
las tablas y relaciones que faltan sin recrear las tablas existentes.

Las pruebas de persistencia usan el perfil `test` y una base H2 en memoria,
independiente de PostgreSQL.
