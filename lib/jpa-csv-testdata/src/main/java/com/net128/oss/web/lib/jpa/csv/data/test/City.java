package com.net128.oss.web.lib.jpa.csv.data.test;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Props.Sortable
public class City extends Identifiable {
	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	@Min(0)
	@Max(99999999)
	private long population;

	@Column(nullable = false)
	private String country;

	@Column(nullable = false)
	@Pattern(regexp="[A-Z][A-Z]")
	private String isoCountry;

	@Column(nullable = false)
	@Min(-180) @Max(180)
	private double latitude;

	@Column(nullable = false)
	@Min(-180) @Max(180)
	private double longitude;

	@Column(nullable = false)
	private long geoId;

	@Column(nullable = false)
	private LocalDate modified;
}
