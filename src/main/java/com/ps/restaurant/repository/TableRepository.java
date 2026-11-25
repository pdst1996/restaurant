package com.ps.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.Table;

public interface TableRepository extends JpaRepository<Table, Long>, JpaSpecificationExecutor<Table> {

    public boolean existsByTableName(String tableName);

}
