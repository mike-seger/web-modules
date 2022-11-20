package com.net128.oss.web.lib.jpa.csv.data.test;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Car extends Identifiable {
	@Column(nullable = false)
	@NotBlank
	private String brand;

	@Column(nullable = false)
	@NotNull
	private double totalPrice;
}
