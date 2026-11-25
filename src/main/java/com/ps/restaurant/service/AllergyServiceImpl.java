package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.AllergyRequest;
import com.ps.restaurant.dto.AllergyResponse;
import com.ps.restaurant.dto.filters.FilterAllergyRequest;
import com.ps.restaurant.interfaces.AllergyService;
import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.repository.AllergyRepository;
import com.ps.restaurant.repository.specification.AllergySpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllergyServiceImpl implements AllergyService {

    private final AllergyRepository repository;

    @Override
    public Optional<Allergy> getAllergyById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<AllergyResponse> filterAllergies(FilterAllergyRequest filter) {
        return repository.findAll(AllergySpecifications.withFilters(filter)).stream()
                .map(AllergyResponse::from)
                .toList();
    }

    @Override
    public long countAllergies(FilterAllergyRequest filter) {
        return repository.count(AllergySpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public AllergyResponse createAllergy(AllergyRequest request) {
        if (repository.existsByAllergyKey(request.allergyKey())) {
            throw new ConflictException("Allergy with this allergyKey already exists");
        }

        Allergy entity = new Allergy();
        entity.setAllergyKey(request.allergyKey());
        entity.setDisplayName(request.displayName());
        return AllergyResponse.from(repository.save(entity));
    }
}

