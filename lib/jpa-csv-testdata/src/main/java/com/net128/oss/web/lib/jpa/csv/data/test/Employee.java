package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.*;

import javax.persistence.*;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
@Table(name = "employee")
public class Employee extends EmployeeBase {
	@ManyToOne
	@JoinColumn(name = "departmentId", referencedColumnName = "departmentId")
	private Department department;
}
