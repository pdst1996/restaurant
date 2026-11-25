package com.ps.restaurant.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ElementTypeRequest(
    @NotBlank @Size(max = 255) String type,
    @NotEmpty Map<@NotBlank  String, @NotBlank  String> label, 
    @NotNull @Positive Integer width,
    @NotNull @Positive Integer height,
    boolean round, 
    @Size(max = 255) String fill,
    @Size(max = 255) String stroke) {

}
