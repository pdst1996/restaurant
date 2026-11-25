package com.ps.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.Allergy;

public interface AllergyRepository extends JpaRepository<Allergy, Long>, JpaSpecificationExecutor<Allergy> {

    public List<Allergy> findByAllergyKey(String allergyKey);

    public boolean existsByAllergyKey(String allergyKey);

}
