package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.*;

import jakarta.persistence.Embeddable;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class Address {
	private String street;
	private String city;
	private String country;
}
