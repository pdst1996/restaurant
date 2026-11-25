package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.RoleRequest;
import com.ps.restaurant.dto.RoleResponse;
import com.ps.restaurant.dto.filters.FilterRoleRequest;
import com.ps.restaurant.interfaces.RoleService;
import com.ps.restaurant.model.Role;
import com.ps.restaurant.repository.RoleRepository;
import com.ps.restaurant.repository.specification.RoleSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;

    @Override
    public Optional<Role> getRoleById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<RoleResponse> filterRoles(FilterRoleRequest filter) {
        return repository.findAll(RoleSpecifications.withFilters(filter)).stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Override
    public long countRoles(FilterRoleRequest filter) {
        return repository.count(RoleSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (repository.existsByName(request.name())) {
            throw new ConflictException("Role with this name already exists");
        }

        Role entity = new Role();
        entity.setName(request.name());
        entity.setActive(request.active());
        return RoleResponse.from(repository.save(entity));
    }
}

