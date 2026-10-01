package com.institutojf.mottainai.repository;

import com.institutojf.mottainai.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    boolean existsByCpf(String cpf);

    long countByRole_IdAndDeletedAtIsNull(Integer roleId);

    List<Employee> findAllByStore_Company_IdAndDeletedAtIsNull(Integer companyId);

}
