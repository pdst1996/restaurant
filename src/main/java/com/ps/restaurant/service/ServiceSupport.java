package com.ps.restaurant.service;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.dto.ErrorResponseException.NotFoundException;

final class ServiceSupport {

    private ServiceSupport() {
    }

    static <T> T requireById(JpaRepository<T, Long> repository, Long id, String resource) {
        if (id == null) {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST.value(), "VALIDATION_EXCEPTION",
                    resource + " id is required");
        }
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(resource + " with id " + id + " was not found"));
    }

    static void validateDateRange(Instant fromDate, Instant toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST.value(), "VALIDATION_EXCEPTION",
                    "fromDate must be before or equal to toDate");
        }
    }
}

