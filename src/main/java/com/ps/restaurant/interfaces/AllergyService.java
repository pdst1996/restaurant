package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.AllergyRequest;
import com.ps.restaurant.dto.AllergyResponse;
import com.ps.restaurant.dto.filters.FilterAllergyRequest;
import com.ps.restaurant.model.Allergy;

public interface AllergyService {

    Optional<Allergy> getAllergyById(Long id);

    List<AllergyResponse> filterAllergies(FilterAllergyRequest filter);

    long countAllergies(FilterAllergyRequest filter);

    AllergyResponse createAllergy(AllergyRequest request);
}

