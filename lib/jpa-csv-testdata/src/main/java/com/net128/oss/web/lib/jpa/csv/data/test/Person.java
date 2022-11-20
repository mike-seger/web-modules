package com.net128.oss.web.lib.jpa.csv.data.test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

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
	@NotBlank
	private String city;

	@Column(nullable = false)
	@NotBlank
	private String address;

	@Column(nullable = false)
	@NotBlank
	private String firstName;

	@Column(nullable = false)
	@NotBlank
	private String lastName;

	@Column(nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private Country country;
}
