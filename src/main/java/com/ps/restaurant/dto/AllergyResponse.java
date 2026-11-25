package com.ps.restaurant.dto;

import java.util.Map;

import com.ps.restaurant.model.Allergy;

/** Exposes an allergy's identifier, key, and localized names. */
public record AllergyResponse(Long id, String allergyKey, Map<String, String> displayName) {

    public static AllergyResponse from(Allergy allergy) {
        return new AllergyResponse(allergy.getId(), allergy.getAllergyKey(), allergy.getDisplayName());
    }
}
