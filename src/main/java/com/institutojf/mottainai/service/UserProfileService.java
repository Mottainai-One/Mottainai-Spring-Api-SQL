package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.InviteStoreUserRequest;
import com.institutojf.mottainai.dto.request.UpdateStoreUserRequest;
import com.institutojf.mottainai.dto.response.InviteStoreUserResponse;
import com.institutojf.mottainai.dto.response.RetailStoreResponse;
import com.institutojf.mottainai.dto.response.UserResponse;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import com.institutojf.mottainai.mapper.RetailStoreMapper;
import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.model.Employee;
import com.institutojf.mottainai.model.EmployeeRole;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.repository.EmployeeRepository;
import com.institutojf.mottainai.repository.EmployeeRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
public class UserProfileService {
    private final AppUserRepository appUserRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final RetailStoreMapper retailStoreMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserProfileService(AppUserRepository appUserRepository, EmployeeRepository employeeRepository, EmployeeRoleRepository employeeRoleRepository, RetailStoreMapper retailStoreMapper, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.employeeRepository = employeeRepository;
        this.employeeRoleRepository = employeeRoleRepository;
        this.retailStoreMapper = retailStoreMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        return toResponse(findUser(email));
    }

    @Transactional(readOnly = true)
    public RetailStoreResponse myStore(String email) {
        return retailStoreMapper.toResponse(findUser(email).getEmployee().getStore());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return appUserRepository.findAllByDeletedAtIsNull().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Integer id) {
        return toResponse(appUserRepository.findById(id).filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Transactional
    public InviteStoreUserResponse invite(InviteStoreUserRequest request, String requesterEmail) {
        AppUser requester = findUser(requesterEmail);
        if (appUserRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw new ConflictException("Email already exists");
        }
        if (employeeRepository.existsByCpf(request.cpf())) {
            throw new ConflictException("CPF already exists");
        }

        Employee employee = new Employee();
        employee.setStore(requester.getEmployee().getStore());
        employee.setRole(findRole(request.role()));
        employee.setName(request.name());
        employee.setCpf(request.cpf());
        employee.setEmail(request.email());
        employee.setPhone(request.phone());
        employee.setActive(false);
        employee = employeeRepository.save(employee);

        AppUser user = new AppUser();
        user.setEmployee(employee);
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(generateRandomSecret()));
        user.setTokenVersion(0);
        user.setActive(false);
        user = appUserRepository.save(user);

        return new InviteStoreUserResponse(toResponse(user), false, true, false);
    }

    @Transactional
    public UserResponse update(Integer id, UpdateStoreUserRequest request) {
        AppUser user = findByIdEntity(id);
        if (request.role() == null && request.active() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }
        if (request.role() != null) {
            user.getEmployee().setRole(findRole(request.role()));
        }
        if (request.active() != null) {
            user.setActive(request.active());
            user.getEmployee().setActive(request.active());
        }
        return toResponse(appUserRepository.save(user));
    }

    private AppUser findUser(String email) {
        return appUserRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(user -> Boolean.TRUE.equals(user.getActive()) && Boolean.TRUE.equals(user.getEmployee().getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AppUser findByIdEntity(Integer id) {
        return appUserRepository.findById(id).filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private EmployeeRole findRole(String role) {
        return employeeRoleRepository.findByNameIgnoreCaseAndActiveTrueAndDeletedAtIsNull(role)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    private UserResponse toResponse(AppUser user) {
        Employee employee = user.getEmployee();
        return new UserResponse(user.getId(), employee.getName(), employee.getCpf(), user.getEmail(),
                employee.getPhone(), employee.getRole().getName(), user.getActive(), employee.getStore().getId(),
                user.getFirebaseUid());
    }

    private String generateRandomSecret() {
        return Long.toUnsignedString(secureRandom.nextLong()) + Long.toUnsignedString(secureRandom.nextLong());
    }
}
