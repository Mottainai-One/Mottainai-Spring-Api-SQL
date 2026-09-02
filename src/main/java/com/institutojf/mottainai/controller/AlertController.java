package com.institutojf.mottainai.controller;

import com.institutojf.mottainai.controller.swagger.AlertControllerApi;

import com.institutojf.mottainai.dto.request.CreateAlertRequest;
import com.institutojf.mottainai.dto.response.AlertResponse;
import com.institutojf.mottainai.service.AlertService;
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
@RequestMapping("/api/v1/alerts")
public class AlertController implements AlertControllerApi {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @Override
    @GetMapping
    public List<AlertResponse> getAlertsByStore(@RequestParam Integer storeId) {
        return alertService.getAlertsByStore(storeId);
    }

    @Override
    @GetMapping("/{id}")
    public AlertResponse getAlertById(@PathVariable Integer id) {
        return alertService.getAlertById(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse createAlert(@Valid @RequestBody CreateAlertRequest request) {
        return alertService.createAlert(request);
    }

    @Override
    @PostMapping("/{id}/resolve")
    public AlertResponse resolveAlert(@PathVariable Integer id) {
        return alertService.resolveAlert(id);
    }
}
