package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Car extends Identifiable {
	@Column(nullable = false)
	private String brand;

	@Column(nullable = false)
	private double totalPrice;
}
