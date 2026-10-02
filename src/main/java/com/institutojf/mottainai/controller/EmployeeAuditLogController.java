package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.EmployeeAuditLogControllerApi;
import com.institutojf.mottainai.dto.response.AuditLogResponse;
import com.institutojf.mottainai.service.AuditLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeAuditLogController implements EmployeeAuditLogControllerApi {
    private final AuditLogService auditLogService;

    public EmployeeAuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<AuditLogResponse>> findByEmployee(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(auditLogService.findByEmployee(id, from, to));
    }
}
