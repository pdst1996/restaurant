package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.RoleRequest;
import com.ps.restaurant.dto.RoleResponse;
import com.ps.restaurant.dto.filters.FilterRoleRequest;
import com.ps.restaurant.interfaces.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/role/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/permissions")
    public List<String> permissions() {
        return com.ps.restaurant.config.Permissions.ALL.stream().sorted().toList();
    }

    @PutMapping("/{id}")
    public RoleResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roleService.updateRole(id, request);
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles(
            @Valid @ModelAttribute FilterRoleRequest filter) {
        return ResponseEntity.ok(roleService.filterRoles(filter));
    }

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.createRole(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getRolesCount(@Valid @ModelAttribute FilterRoleRequest filter) {
        return ResponseEntity.ok(roleService.countRoles(filter));
    }
}

