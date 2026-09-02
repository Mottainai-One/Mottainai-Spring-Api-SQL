package com.institutojf.mottainai.service;

import com.institutojf.mottainai.dto.request.InviteStoreUserRequest;
import com.institutojf.mottainai.dto.request.UpdateStoreUserRequest;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.model.Employee;
import com.institutojf.mottainai.model.EmployeeRole;
import com.institutojf.mottainai.model.RetailStore;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.institutojf.mottainai.repository.EmployeeRepository;
import com.institutojf.mottainai.repository.EmployeeRoleRepository;
import com.institutojf.mottainai.mapper.RetailStoreMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock AppUserRepository appUserRepository;

    @Mock EmployeeRepository employeeRepository;

    @Mock EmployeeRoleRepository employeeRoleRepository;

    @Mock RetailStoreMapper retailStoreMapper;

    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserProfileService service;

    @Test
    void shouldReturnAuthenticatedUserWithNullableFirebaseUid() {
        AppUser user = user(1, "ADMINISTRATOR", true);
        when(appUserRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@test.com")).thenReturn(Optional.of(user));

        var response = service.me("admin@test.com");

        assertEquals("admin@test.com", response.email());
        assertNull(response.firebaseUid());
        assertEquals("ADMINISTRATOR", response.role());
    }

    @Test
    void shouldCreateInactiveInviteForAuthenticatedUsersStore() {
        AppUser requester = user(1, "ADMINISTRATOR", true);
        EmployeeRole role = role("OPERATOR");
        when(appUserRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@test.com")).thenReturn(Optional.of(requester));
        when(appUserRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("new@test.com")).thenReturn(false);
        when(employeeRepository.existsByCpf("12345678901")).thenReturn(false);
        when(employeeRoleRepository.findByNameIgnoreCaseAndActiveTrueAndDeletedAtIsNull("OPERATOR")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.invite(new InviteStoreUserRequest("New User", "12345678901", "new@test.com", null, "OPERATOR"), "admin@test.com");

        assertFalse(response.active());
        assertTrue(response.passwordSetupRequired());
        assertFalse(response.emailNotificationSent());
        verify(passwordEncoder).encode(anyString());
        verify(appUserRepository).save(argThat(user -> !user.getActive() && user.getEmployee().getStore() == requester.getEmployee().getStore()));
    }

    @Test
    void shouldRejectExistingEmailBeforeCreatingInvite() {
        AppUser requester = user(1, "ADMINISTRATOR", true);
        when(appUserRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@test.com")).thenReturn(Optional.of(requester));
        when(appUserRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("new@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.invite(
                new InviteStoreUserRequest("New User", "12345678901", "new@test.com", null, "OPERATOR"), "admin@test.com"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void shouldUpdateRoleAndStatusTogether() {
        AppUser user = user(2, "OPERATOR", false);
        EmployeeRole role = role("SUPERVISOR");
        when(appUserRepository.findById(2)).thenReturn(Optional.of(user));
        when(employeeRoleRepository.findByNameIgnoreCaseAndActiveTrueAndDeletedAtIsNull("SUPERVISOR")).thenReturn(Optional.of(role));
        when(appUserRepository.save(user)).thenReturn(user);

        var response = service.update(2, new UpdateStoreUserRequest("SUPERVISOR", true));

        assertEquals("SUPERVISOR", response.role());
        assertTrue(response.active());
        assertTrue(user.getEmployee().getActive());
    }

    private AppUser user(int id, String roleName, boolean active) {
        RetailStore store = new RetailStore();
        store.setId(7);
        Employee employee = new Employee();
        employee.setStore(store);
        employee.setName("User");
        employee.setCpf("12345678901");
        employee.setActive(active);
        employee.setRole(role(roleName));
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(id == 1 ? "admin@test.com" : "user@test.com");
        user.setEmployee(employee);
        user.setActive(active);
        return user;
    }

    private EmployeeRole role(String name) {
        EmployeeRole role = new EmployeeRole();
        role.setName(name);
        role.setActive(true);
        return role;
    }
}
