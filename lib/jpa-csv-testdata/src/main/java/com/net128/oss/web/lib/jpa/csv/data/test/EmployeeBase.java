package com.net128.oss.web.lib.jpa.csv.data.test;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Entity
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
