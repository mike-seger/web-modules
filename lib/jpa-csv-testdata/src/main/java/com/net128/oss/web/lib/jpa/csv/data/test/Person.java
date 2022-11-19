package com.net128.oss.web.lib.jpa.csv.data.test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.*;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({"myId","city","address","firstName","lastName","country"})
public class Person {
	@Props.Hidden
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long myId;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Country country;
}
