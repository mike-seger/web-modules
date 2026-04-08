package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Department extends Identifiable {
	@Column(nullable = false, unique = true)
	@NotNull
	private Long departmentId;

	@Column(nullable = false, unique = true)
	@NotBlank
	private String name;
}
