package com.net128.oss.web.app.jpa.csv.testdata.main;

import com.net128.oss.web.lib.jpa.csv.data.test.Employee;
import com.net128.oss.web.lib.jpa.csv.data.test.EmployeeRepository;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping(("/api/"))
public class EmployeeController {
	private final EmployeeService employeeService;
	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}
	@GetMapping("employees")
	public List<Employee> loadEmployees() {
		return employeeService.loadAll();
	}

	@PostMapping("employees")
	public void saveEmployees(@RequestBody List<Employee> employees) {
		employeeService.deleteAll();
		employeeService.saveAll(employees);
	}
}
