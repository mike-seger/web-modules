package com.net128.oss.web.lib.jpa.csv.data.test;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Employee extends Identifiable {
	@Column(nullable = false)
	@NotBlank
	private String name;

	@Column(nullable = false)
	@NotNull
	@PositiveOrZero
	private double salary;

	@ManyToOne
	@JoinColumn(referencedColumnName = "name")
	private Department department;
}
