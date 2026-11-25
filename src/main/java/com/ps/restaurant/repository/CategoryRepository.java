package com.ps.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ps.restaurant.model.ItemCategory;

public interface CategoryRepository extends JpaRepository<ItemCategory, Long>, JpaSpecificationExecutor<ItemCategory> {

    @Query(value = """
        SELECT *
        FROM categories c
        WHERE c.display_name ->> :language = :value
        """, nativeQuery = true)
    List<ItemCategory> findByDisplayName(
        @Param("language") String language,
        @Param("value") String value
    );

    List<ItemCategory> findByCategoryKey(String categoryKey);

    boolean existsByCategoryKey(String categoryKey);

}
