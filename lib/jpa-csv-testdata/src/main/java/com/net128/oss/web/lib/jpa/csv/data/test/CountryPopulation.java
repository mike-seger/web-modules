package com.net128.oss.web.lib.jpa.csv.data.test;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Props.Sortable
public class CountryPopulation extends Identifiable {
	@Column(nullable = false)
	private String country;

	@Column(nullable = false)
	@PositiveOrZero
	private long population;

	@Column(nullable = false)
	@Min(-100)
	private Double yearChangePerc;

	@Column(nullable = false)
	private int netChange;

	@Column(nullable = false)
	@PositiveOrZero
	private int popPerSqKm;

	@Column(nullable = false)
	@PositiveOrZero
	private int areaSqKm;

	//TODO check if serialization of null leads to NaM which cannot be deserialized
	@Column
	private Integer migrants;

	@Column
	@PositiveOrZero
	private Double fertRate;

	@Column
	@Min(0) @Max(100)
	private Integer medAge;

	@Column
	@Min(0) @Max(100)
	private Double popUrbanPerc;

	@Column(nullable = false)
	@Min(0) @Max(100)
	private double worldPerc;
}
