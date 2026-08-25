package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.response.CepResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.CepNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Consulta CEP na BrasilAPI
 * O resultado fica em cache por 10 minutos pra não ficar chamando a API toda hora
 */
@Slf4j
@Service
public class BrasilApiService {

    private final RestClient restClient;

    public BrasilApiService(@Value("${brasilapi.base-url:https://brasilapi.com.br/api}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Cacheable(cacheNames = "cep", key = "#cep")
    public CepResponse getCep(String cep) {
        try {
            CepApiResult result = restClient.get()
                    .uri("/cep/v2/{cep}", cep)
                    .retrieve()
                    .body(CepApiResult.class);

            if (result == null) {
                log.error("CEP {} not found in BrasilAPI (null response)", cep);
                throw new CepNotFoundException(cep);
            }

            return new CepResponse(
                    result.cep(),
                    result.street(),
                    result.neighborhood(),
                    result.city(),
                    result.state(),
                    result.service()
            );

        } catch (RestClientResponseException exception) {
            int statusCode = exception.getStatusCode().value();

            if (statusCode == 404) {
                log.error("CEP {} not found in BrasilAPI (404)", cep);
                throw new CepNotFoundException(cep);
            }

            log.error("BrasilAPI returned error for CEP {}: status {} - {}", cep, statusCode, exception.getMessage());
            throw new BusinessException("Postal service error: " + statusCode);

        } catch (CepNotFoundException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error("Unexpected error fetching CEP {}: {}", cep, exception.getMessage());
            throw new BusinessException("Failed to fetch CEP data");
        }
    }

    private record CepApiResult(
            String cep,
            String street,
            String neighborhood,
            String city,
            String state,
            String service
    ) {}
}