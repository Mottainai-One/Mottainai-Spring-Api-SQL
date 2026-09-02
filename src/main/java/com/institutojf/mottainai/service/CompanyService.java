package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.CreateCompanyRequest;
import com.institutojf.mottainai.dto.request.UpdateCompanyRequest;
import com.institutojf.mottainai.dto.response.CompanyResponse;
import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.CompanyMapper;
import com.institutojf.mottainai.model.Company;
import com.institutojf.mottainai.model.SubscriptionPlan;
import com.institutojf.mottainai.repository.CompanyRepository;
import com.institutojf.mottainai.repository.RetailStoreRepository;
import com.institutojf.mottainai.repository.SubscriptionPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final RetailStoreRepository retailStoreRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, SubscriptionPlanRepository subscriptionPlanRepository, RetailStoreRepository retailStoreRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.retailStoreRepository = retailStoreRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        if (companyRepository.existsByCnpj(request.cnpj())) {
            throw new ConflictException("Company CNPJ already exists");
        }

        Company company = new Company();
        company.setCnpj(request.cnpj());
        company.setActive(true);
        applyCompanyFields(
                request.planId(), request.officialName(), request.tradeName(),
                request.email(), request.phone(), request.latitude(), request.longitude(), company
        );

        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> findAll(Pageable pageable) {
        return companyRepository.findAllByActiveTrueAndDeletedAtIsNull(pageable).map(companyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Integer id) {
        return companyMapper.toResponse(findActiveCompanyById(id));
    }

    @Transactional
    public CompanyResponse update(Integer id, UpdateCompanyRequest request) {
        Company company = findCompanyById(id);
        if (Boolean.FALSE.equals(request.active())) {
            ensureCanDeactivate(id);
        }
        applyCompanyFields(
                request.planId(), request.officialName(), request.tradeName(),
                request.email(), request.phone(), request.latitude(), request.longitude(), company
        );
        company.setActive(request.active());

        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Transactional
    public void deactivate(Integer id) {
        Company company = findActiveCompanyById(id);
        ensureCanDeactivate(id);
        company.setActive(false);
        companyRepository.save(company);
    }

    private void ensureCanDeactivate(Integer id) {
        if (retailStoreRepository.existsByCompany_IdAndActiveTrueAndDeletedAtIsNull(id)) {
            throw new BusinessException("Company has active stores");
        }
    }

    private Company findActiveCompanyById(Integer id) {
        return companyRepository.findByIdAndActiveTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    private Company findCompanyById(Integer id) {
        return companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    /**
     * Aplica os dados editáveis da empresa.
     * A troca de plano só é permitida se a quantidade de lojas ativas couber no limite do novo plano,
     * evitando que um downgrade deixe a empresa em situação irregular.
     */
    private void applyCompanyFields(
            Integer planId, String officialName, String tradeName,
            String email, String phone, java.math.BigDecimal latitude, java.math.BigDecimal longitude, Company company
    ) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndActiveTrueAndDeletedAtIsNull(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (company.getId() != null) {
            long activeStores = retailStoreRepository.countByCompany_IdAndActiveTrueAndDeletedAtIsNull(company.getId());
            if (activeStores > plan.getStoreLimit()) {
                throw new BusinessException("Company has more active stores than the plan allows");
            }
        }

        company.setPlan(plan);
        company.setOfficialName(officialName);
        company.setTradeName(tradeName);
        company.setEmail(email);
        company.setPhone(phone);
        company.setLatitude(latitude);
        company.setLongitude(longitude);
    }
}
