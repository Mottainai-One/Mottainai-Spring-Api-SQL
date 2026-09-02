package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateAlertRequest;
import com.institutojf.mottainai.dto.response.AlertResponse;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.Alert;
import com.institutojf.mottainai.model.enums.AlertStatus;
import com.institutojf.mottainai.model.RetailStore;
import com.institutojf.mottainai.repository.AlertRepository;
import com.institutojf.mottainai.repository.RetailStoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final RetailStoreRepository retailStoreRepository;

    public AlertService(AlertRepository alertRepository, RetailStoreRepository retailStoreRepository) {
        this.alertRepository = alertRepository;
        this.retailStoreRepository = retailStoreRepository;
    }

    @Transactional
    public AlertResponse createAlert(CreateAlertRequest request) {
        RetailStore store = retailStoreRepository.findById(request.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        Alert alert = new Alert();
        alert.setStore(store);
        alert.setTitle(request.title());
        alert.setDescription(request.description());
        alert.setAlertType(request.alertType());
        alert.setPriority(request.priority());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setGeneratedAt(LocalDateTime.now());
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        return AlertResponse.fromEntity(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByStore(Integer storeId) {
        return alertRepository.findByStore_StoreIdOrderByGeneratedAtDesc(storeId).stream()
                .map(AlertResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlertById(Integer id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        return AlertResponse.fromEntity(alert);
    }

    @Transactional
    public AlertResponse resolveAlert(Integer id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        return AlertResponse.fromEntity(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public long countActiveAlerts(Integer storeId) {
        return alertRepository.countByStore_StoreIdAndStatus(storeId, AlertStatus.ACTIVE);
    }
}
