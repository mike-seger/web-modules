package com.net128.oss.web.lib.jpa.csv.model;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.Data;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
@Data
public abstract class Identifiable {
	@Props.Hidden
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Identifiable)) return false;
		Identifiable that = (Identifiable) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
