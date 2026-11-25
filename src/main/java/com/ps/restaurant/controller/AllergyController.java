package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.AllergyRequest;
import com.ps.restaurant.dto.AllergyResponse;
import com.ps.restaurant.dto.filters.FilterAllergyRequest;
import com.ps.restaurant.interfaces.AllergyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/allergy/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyService allergyService;

    @GetMapping
    public ResponseEntity<List<AllergyResponse>> getAllergies(
            @Valid @ModelAttribute FilterAllergyRequest filter) {
        return ResponseEntity.ok(allergyService.filterAllergies(filter));
    }

    @PostMapping
    public ResponseEntity<AllergyResponse> createAllergy(@Valid @RequestBody AllergyRequest request) {
        return ResponseEntity.ok(allergyService.createAllergy(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getAllergiesCount(@Valid @ModelAttribute FilterAllergyRequest filter) {
        return ResponseEntity.ok(allergyService.countAllergies(filter));
    }
}

