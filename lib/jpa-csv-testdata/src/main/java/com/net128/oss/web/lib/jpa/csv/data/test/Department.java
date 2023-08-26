package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Department extends Identifiable {
	@Column(nullable = false, unique = true)
	@NotBlank
	private Long departmentId;

	@Column(nullable = false)
	@NotBlank
	private String name;
}
