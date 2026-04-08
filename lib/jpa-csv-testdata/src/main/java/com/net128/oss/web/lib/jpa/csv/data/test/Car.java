package com.net128.oss.web.lib.jpa.csv.data.test;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Car extends Identifiable {
	@Column(nullable = false)
	@NotBlank
	private String brand;

	@Column(nullable = false)
	@NotNull
	@PositiveOrZero
	private double totalPrice;
}
