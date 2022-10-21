package com.net128.oss.web.lib.jpa.csv.pet.repo;

import com.net128.oss.web.lib.jpa.csv.pet.model.Pet;
import com.net128.oss.web.lib.jpa.csv.pet.model.Species;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findBySpeciesId(long id);
    default List<Pet> filter(Species species) {
        List<Pet> pets;
        if(species == null) pets = findAll();
        else pets = findBySpeciesId(species.getId());
        return pets;
    }

    //@Query("select p from Pet p order by p.name")
    default List<Pet> findAllOrdered() {
        var pets = findAll();
        return pets;
    }
}
