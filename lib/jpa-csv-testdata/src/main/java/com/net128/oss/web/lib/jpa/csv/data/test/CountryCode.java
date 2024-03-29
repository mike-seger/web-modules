package com.net128.oss.web.lib.jpa.csv.data.test;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Pattern;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Props.Sortable
public class CountryCode extends Audited {
	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Pattern(regexp="[A-Z]{2}")
	private String alpha2;

	@Column(nullable = false)
	@Pattern(regexp="[A-Z]{3}")
	private String alpha3;

	@Column(nullable = false)
	private String region;

	@Column(nullable = false)
	private String subRegion;

	@Column(nullable = false)
	private String regionCode;

	@Column(nullable = false)
	private String subRegionCode;
}
