package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.EmployeeRoleControllerApi;
import com.institutojf.mottainai.dto.request.EmployeeRoleRequest;
import com.institutojf.mottainai.dto.response.EmployeeRoleResponse;
import com.institutojf.mottainai.service.EmployeeRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-roles")
@RequiredArgsConstructor
public class EmployeeRoleController implements EmployeeRoleControllerApi {
    private final EmployeeRoleService employeeRoleService;

    @Override
    @GetMapping
    public ResponseEntity<List<EmployeeRoleResponse>> findAll() {
        return ResponseEntity.ok(employeeRoleService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeRoleResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeRoleService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<EmployeeRoleResponse> create(@Valid @RequestBody EmployeeRoleRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeRoleService.create(request, authentication.getName()));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<EmployeeRoleResponse> update(@PathVariable Integer id, @Valid @RequestBody EmployeeRoleRequest request, Authentication authentication) {
        return ResponseEntity.ok(employeeRoleService.update(id, request, authentication.getName()));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id, Authentication authentication) {
        employeeRoleService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
