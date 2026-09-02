package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    boolean existsByCpf(String cpf);
}
