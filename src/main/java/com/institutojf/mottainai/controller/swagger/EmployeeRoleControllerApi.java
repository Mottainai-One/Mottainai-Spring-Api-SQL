package com.institutojf.mottainai.controller.swagger;

import com.institutojf.mottainai.dto.request.EmployeeRoleRequest;
import com.institutojf.mottainai.dto.response.EmployeeRoleResponse;
import com.institutojf.mottainai.handler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@Tag(name = "Employee Roles", description = "Manage employee role data. API authorization currently uses role names; permission levels are returned as metadata.")
public interface EmployeeRoleControllerApi {

    @Operation(summary = "List active employee roles")
    @ApiResponse(responseCode = "200", description = "Roles found", content = @Content(schema = @Schema(implementation = EmployeeRoleResponse.class)))
    ResponseEntity<List<EmployeeRoleResponse>> findAll();

    @Operation(summary = "Get an employee role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role found", content = @Content(schema = @Schema(implementation = EmployeeRoleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<EmployeeRoleResponse> findById(Integer id);

    @Operation(summary = "Create an employee role")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Role created", content = @Content(schema = @Schema(implementation = EmployeeRoleResponse.class))),
            @ApiResponse(responseCode = "409", description = "Role name already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<EmployeeRoleResponse> create(EmployeeRoleRequest request, Authentication authentication);

    @Operation(summary = "Update employee role data. Permission level does not change API authorities.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated", content = @Content(schema = @Schema(implementation = EmployeeRoleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Authorization role name cannot be changed", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<EmployeeRoleResponse> update(Integer id, EmployeeRoleRequest request, Authentication authentication);

    @Operation(summary = "Deactivate an employee role")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Role deactivated"),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    ResponseEntity<Void> delete(Integer id, Authentication authentication);
}
