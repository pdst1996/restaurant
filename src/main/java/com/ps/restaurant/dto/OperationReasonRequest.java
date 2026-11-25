package com.ps.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OperationReasonRequest(@NotBlank @Size(max = 500) String reason) {}
