package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.RoleRequest;
import com.ps.restaurant.dto.RoleResponse;
import com.ps.restaurant.dto.filters.FilterRoleRequest;
import com.ps.restaurant.model.Role;

public interface RoleService {

    Optional<Role> getRoleById(Long id);

    List<RoleResponse> filterRoles(FilterRoleRequest filter);

    long countRoles(FilterRoleRequest filter);

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);
}

