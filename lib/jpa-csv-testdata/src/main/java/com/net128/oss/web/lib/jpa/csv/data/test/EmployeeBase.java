package com.net128.oss.web.lib.jpa.csv.data.test;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
@MappedSuperclass
public abstract class EmployeeBase extends Identifiable {
	@Column(nullable = false)
	@NotBlank
	private String name;

	@Column(nullable = false)
	@NotNull
	@PositiveOrZero
	private double salary;
}
