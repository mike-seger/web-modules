package com.net128.oss.web.lib.jpa.csv.data.test;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
	List<City> findAllByOrderByPopulationDescCity();
	default List<City> findAll() { return findAllByOrderByPopulationDescCity(); }
}
