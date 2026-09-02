package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.SuggestedActionControllerApi;

import com.institutojf.mottainai.dto.request.CreateSuggestedActionRequest;
import com.institutojf.mottainai.dto.response.SuggestedActionResponse;
import com.institutojf.mottainai.service.SuggestedActionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suggestions")
public class SuggestedActionController implements SuggestedActionControllerApi {

    private final SuggestedActionService suggestedActionService;

    public SuggestedActionController(SuggestedActionService suggestedActionService) {
        this.suggestedActionService = suggestedActionService;
    }

    @Override
    @GetMapping
    public List<SuggestedActionResponse> getActionsByStore(@RequestParam Integer storeId) {
        return suggestedActionService.getActionsByStore(storeId);
    }

    @Override
    @GetMapping("/{id}")
    public SuggestedActionResponse getActionById(@PathVariable Integer id) {
        return suggestedActionService.getActionById(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuggestedActionResponse createSuggestedAction(@Valid @RequestBody CreateSuggestedActionRequest request) {
        return suggestedActionService.createSuggestedAction(request);
    }

    @Override
    @PostMapping("/{id}/approve")
    public SuggestedActionResponse approveAction(@PathVariable Integer id) {
        return suggestedActionService.approveAction(id);
    }

    @Override
    @PostMapping("/{id}/reject")
    public SuggestedActionResponse rejectAction(@PathVariable Integer id) {
        return suggestedActionService.rejectAction(id);
    }
}
