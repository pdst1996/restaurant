package com.ps.restaurant.model;

/** Lists persisted order states without enforcing transitions; Java generates values() and valueOf(String). */
public enum OrderStatus {
    OPEN,
    CONFIRMED,
    PREPARING,
    READY,
    SERVED,
    PAID,
    CANCELLED
}
