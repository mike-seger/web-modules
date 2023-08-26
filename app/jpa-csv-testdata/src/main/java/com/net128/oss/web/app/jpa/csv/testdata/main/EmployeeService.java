package com.net128.oss.web.app.jpa.csv.testdata.main;

import com.net128.oss.web.lib.jpa.csv.data.test.Employee;
import com.net128.oss.web.lib.jpa.csv.data.test.EmployeeRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class EmployeeService {
	private final EmployeeRepository employeeRepository;
	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public List<Employee> loadAll() {
		return employeeRepository.findAll();
	}

	@Transactional
	public void saveAll(List<Employee> employees) {
		employeeRepository.saveAll(employees);
	}

	@Transactional
	public void deleteAll() {
		employeeRepository.deleteAll();
	}
}
