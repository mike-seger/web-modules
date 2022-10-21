package com.net128.oss.web.lib.jpa.csv.data.test;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryCodeRepository extends JpaRepository<CountryCode, Long> {
	List<CountryCode> findAllByOrderByAlpha3();
	default List<CountryCode> findAll() { return findAllByOrderByAlpha3(); }
}
