package com.ps.restaurant.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Permissions {
    private Permissions() {}

    public static final String ORDER_VIEW = "orders:view";
    public static final String ORDER_CREATE = "orders:create";
    public static final String ORDER_EDIT = "orders:edit";
    public static final String ORDER_ASSIGN = "orders:assign";
    public static final String ORDER_STATUS = "orders:update_status";
    public static final String ORDER_PREPARE = "orders:prepare";
    public static final String ORDER_SERVE = "orders:serve";
    public static final String ORDER_COMPLETE = "orders:complete";
    public static final String ORDER_CANCEL = "orders:cancel";
    public static final String PAYMENT_VIEW = "payments:view";
    public static final String PAYMENT_CREATE = "payments:create";
    public static final String PAYMENT_REFUND = "payments:refund";
    public static final String USER_MANAGE = "users:manage";
    public static final String ROLE_MANAGE = "roles:manage";
    public static final String ITEM_VIEW = "items:view";
    public static final String ITEM_MANAGE = "items:manage";
    public static final String FLOOR_VIEW = "floor:view";
    public static final String FLOOR_MANAGE = "floor:manage";
    public static final String REPORT_VIEW = "reports:view";

    public static final Set<String> ALL = Set.of(ORDER_VIEW, ORDER_CREATE, ORDER_EDIT, ORDER_ASSIGN, ORDER_STATUS,
            ORDER_PREPARE, ORDER_SERVE, ORDER_COMPLETE, ORDER_CANCEL, PAYMENT_VIEW, PAYMENT_CREATE,
            PAYMENT_REFUND, USER_MANAGE, ROLE_MANAGE, ITEM_VIEW, ITEM_MANAGE, FLOOR_VIEW, FLOOR_MANAGE, REPORT_VIEW);

    public static Set<String> defaults(String role) {
        return switch (role) {
            case "admin" -> ALL;
            case "gerente", "manager" -> ALL.stream()
                    .filter(p -> !List.of(USER_MANAGE, ROLE_MANAGE).contains(p)).collect(Collectors.toUnmodifiableSet());
            case "mesero", "waiter" -> Set.of(ORDER_VIEW, ORDER_CREATE, ORDER_EDIT, ORDER_STATUS,
                    ORDER_SERVE, ORDER_CANCEL, ITEM_VIEW, FLOOR_VIEW);
            case "cocina", "cocker" -> Set.of(ORDER_VIEW, ORDER_STATUS, ORDER_PREPARE, ITEM_VIEW, FLOOR_VIEW);
            case "cajero" -> Set.of(ORDER_VIEW, ORDER_STATUS, ORDER_COMPLETE, PAYMENT_VIEW, PAYMENT_CREATE,
                    ITEM_VIEW, FLOOR_VIEW);
            default -> Set.of();
        };
    }

    public static boolean has(org.springframework.security.core.Authentication auth, String permission) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> permission.equals(a.getAuthority()));
    }
}
