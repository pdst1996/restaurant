package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.TableRequest;
import com.ps.restaurant.dto.TableResponse;
import com.ps.restaurant.dto.filters.FilterTableRequest;
import com.ps.restaurant.model.Table;

public interface TableService {
     /**
     * Returns the table with the given identifier, or empty if it does not exist.
     */
    Optional<Table> getTableById(Long id);

    /**
     * Get the Tables filtered
     *
     * @param filter the FilterTableRequest filter
     * @return a list of Tables filtered
     */
    public List<TableResponse> filterTables(FilterTableRequest filter);

    long countTables(FilterTableRequest filter);

    /**
     * Save the Table
     *
     * @param tableRequest
     * @return the Table created
     */
    public TableResponse createTable(TableRequest tableRequest);
}
