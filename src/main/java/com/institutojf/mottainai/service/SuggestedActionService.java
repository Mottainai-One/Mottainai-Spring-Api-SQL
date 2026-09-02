package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateSuggestedActionRequest;
import com.institutojf.mottainai.dto.response.SuggestedActionResponse;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.model.Alert;
import com.institutojf.mottainai.model.SuggestedAction;
import com.institutojf.mottainai.model.enums.SuggestedActionStatus;
import com.institutojf.mottainai.repository.AlertRepository;
import com.institutojf.mottainai.repository.SuggestedActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SuggestedActionService {

    private final SuggestedActionRepository suggestedActionRepository;
    private final AlertRepository alertRepository;

    public SuggestedActionService(SuggestedActionRepository suggestedActionRepository, AlertRepository alertRepository) {
        this.suggestedActionRepository = suggestedActionRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional
    public SuggestedActionResponse createSuggestedAction(CreateSuggestedActionRequest request) {
        Alert alert = alertRepository.findById(request.alertId())
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        SuggestedAction action = new SuggestedAction();
        action.setAlert(alert);
        action.setActionType(request.actionType());
        action.setDescription(request.description());
        action.setPriority(request.priority());
        action.setStatus(SuggestedActionStatus.PENDING);
        action.setGeneratedAt(LocalDateTime.now());
        action.setCreatedAt(LocalDateTime.now());
        action.setUpdatedAt(LocalDateTime.now());

        return SuggestedActionResponse.fromEntity(suggestedActionRepository.save(action));
    }

    @Transactional(readOnly = true)
    public List<SuggestedActionResponse> getActionsByStore(Integer storeId) {
        return suggestedActionRepository.findByAlert_Store_StoreIdOrderByGeneratedAtDesc(storeId).stream()
                .map(SuggestedActionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SuggestedActionResponse getActionById(Integer id) {
        SuggestedAction action = suggestedActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggested action not found"));
        return SuggestedActionResponse.fromEntity(action);
    }

    @Transactional
    public SuggestedActionResponse approveAction(Integer id) {
        SuggestedAction action = suggestedActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggested action not found"));

        action.setStatus(SuggestedActionStatus.APPROVED);
        action.setUpdatedAt(LocalDateTime.now());

        return SuggestedActionResponse.fromEntity(suggestedActionRepository.save(action));
    }

    @Transactional
    public SuggestedActionResponse rejectAction(Integer id) {
        SuggestedAction action = suggestedActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggested action not found"));

        action.setStatus(SuggestedActionStatus.REJECTED);
        action.setUpdatedAt(LocalDateTime.now());

        return SuggestedActionResponse.fromEntity(suggestedActionRepository.save(action));
    }
}
