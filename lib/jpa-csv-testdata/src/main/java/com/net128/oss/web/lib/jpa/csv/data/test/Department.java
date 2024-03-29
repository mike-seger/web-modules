package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
