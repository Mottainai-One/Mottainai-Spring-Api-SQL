package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.response.AuditLogResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByEmployee(Integer employeeId, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null || to.isBefore(from) || to.isAfter(from.plusMonths(6))) {
            throw new BusinessException("A valid audit date range of at most six months is required");
        }
        return auditLogRepository.findByEmployeeIdAndOperationDateBetween(employeeId, from, to);
    }
}
