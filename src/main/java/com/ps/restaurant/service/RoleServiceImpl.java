package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.ps.restaurant.config.Permissions;
import com.ps.restaurant.dto.ErrorResponseException;

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
        apply(entity, request);
        return RoleResponse.from(repository.save(entity));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = ServiceSupport.requireById(repository, id, "Role");
        if ("admin".equals(role.getName())) throw new ConflictException("The system admin role is immutable");
        if (!role.getName().equals(request.name()) && repository.existsByName(request.name())) {
            throw new ConflictException("Role with this name already exists");
        }
        if (Set.of("gerente", "mesero", "cocina", "cajero", "manager", "waiter", "cocker", "cleaner")
                .contains(role.getName()) && !role.getName().equals(request.name())) {
            throw new ConflictException("System role names cannot be changed");
        }
        apply(role, request);
        return RoleResponse.from(role);
    }

    private void apply(Role role, RoleRequest request) {
        if ("admin".equalsIgnoreCase(request.name().trim())) throw new ConflictException("Reserved role name");
        if (request.permissions() != null && !Permissions.ALL.containsAll(request.permissions())) {
            throw new ErrorResponseException(422, "INVALID_PERMISSION", "Unknown permission");
        }
        role.setName(request.name());
        role.setActive(request.active());
        if (request.permissions() != null) {
            role.getPermissions().clear();
            role.getPermissions().addAll(request.permissions());
        }
        role.setPermissionsInitialized(true);
    }
}

