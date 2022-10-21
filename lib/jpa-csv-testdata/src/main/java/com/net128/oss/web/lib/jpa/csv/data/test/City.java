package com.net128.oss.web.lib.jpa.csv.data.test;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
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
	private long population;

	@Column(nullable = false)
	private String country;

	@Column(nullable = false)
	private String isoCountry;

	@Column(nullable = false)
	private double latitude;

	@Column(nullable = false)
	private double longitude;

	@Column(nullable = false)
	private long geoId;

	@Column(nullable = false)
	private LocalDate modified;
}
