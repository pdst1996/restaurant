package com.ps.restaurant.dto;

import com.ps.restaurant.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderTransitionRequest(@NotNull OrderStatus status) {}
