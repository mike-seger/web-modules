package com.net128.oss.web.lib.jpa.csv.data.test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
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
	private Address address;

	@Column(nullable = false)
	@NotBlank
	private String firstName;

	@Column(nullable = false)
	@NotBlank
	private String lastName;
}
