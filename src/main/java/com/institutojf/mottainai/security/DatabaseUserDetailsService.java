package com.institutojf.mottainai.security;

import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.model.Employee;
import com.institutojf.mottainai.model.EmployeeRole;
import com.institutojf.mottainai.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public DatabaseUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser user = appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(email)
                .filter(this::hasActiveEmployment)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        String role = user.getEmployee().getRole().getName().toUpperCase(Locale.ROOT);
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + role)
                .build();
    }

    private boolean hasActiveEmployment(AppUser user) {
        Employee employee = user.getEmployee();
        EmployeeRole role = employee.getRole();
        return Boolean.TRUE.equals(employee.getActive())
                && employee.getDeletedAt() == null
                && Boolean.TRUE.equals(role.getActive())
                && role.getDeletedAt() == null;
    }
}
