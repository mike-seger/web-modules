package com.net128.oss.web.lib.jpa.csv.data.test;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Data
@Props.TailFieldOrder
public abstract class Audited extends Identifiable {
	@Column
	private Instant modified;

	@SuppressWarnings("unused")
	@PreUpdate
	public void change() {
		modified = Instant.now();
	}
}
