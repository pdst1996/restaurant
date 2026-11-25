# Seeder de órdenes y pagos

`OrderSeeder` se ejecuta al iniciar la aplicación cuando `app.seed.enabled=true`,
igual que `UserSeeder`, después de los seeders de usuarios, mesas y productos.

Genera 20 órdenes pagadas con folios `SEED-ORD-001` a `SEED-ORD-020`:

- 10 órdenes con un pago.
- 5 órdenes con dos pagos.
- 5 órdenes con tres pagos.

En total son 35 pagos, usando efectivo (`CASH`), tarjeta (`CARD`) y transferencia
(`TRANSFER`). Cada orden contiene de uno a tres renglones de productos existentes,
con sus cantidades, tamaños y precios. Incluye órdenes en mesa y para llevar,
fechadas durante los 20 días anteriores al arranque.

La suma de los pagos coincide exactamente con el total de la orden; cualquier
centavo restante al dividir se asigna al último pago. Todas quedan en estado `PAID`.

Se reutiliza la relación uno-a-muchos existente: `payments.order_id` apunta a
`orders.id`. No se requiere tabla intermedia ni repositorio separado de pagos;
JPA guarda los productos de la orden y sus pagos en cascada, en una transacción.

Los folios existentes se omiten sin modificar sus datos ni agregarles pagos.
Reiniciar la aplicación no duplica los registros. Para desactivar todos los
seeders, configurar `app.seed.enabled=false`.
