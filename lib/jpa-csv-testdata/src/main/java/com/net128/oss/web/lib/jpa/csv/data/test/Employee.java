package com.net128.oss.web.lib.jpa.csv.data.test;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.*;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
@Table(name = "employee")
//@Props.Hidden
public class Employee extends EmployeeBase {
	@JsonUnwrapped(prefix = "department.")
	@ManyToOne(cascade = CascadeType.ALL, targetEntity = Department.class)
	@JoinColumn(name = "departmentId", referencedColumnName = "departmentId")
	private Department department;
}
