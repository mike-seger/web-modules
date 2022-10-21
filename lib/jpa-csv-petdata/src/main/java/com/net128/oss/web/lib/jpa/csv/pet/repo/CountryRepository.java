package com.net128.oss.web.lib.jpa.csv.pet.repo;

import com.net128.oss.web.lib.jpa.csv.pet.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.StringUtils;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByNameContainingIgnoreCaseOrderById(String name);

    List<Country> findByOrderByNameAsc();
    default List<Country> findAllOrdered() {
        return findByOrderByNameAsc();
    }
    default List<Country> filter(String name) {
        List<Country> countries;
        if (StringUtils.hasText(name)) {
            countries = findByNameContainingIgnoreCaseOrderById(name);
        } else {
            countries = findAllOrdered();
        }
        return countries;
    }
}
