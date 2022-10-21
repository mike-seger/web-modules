package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.net128.oss.web.lib.jpa.csv.util.RefMapping;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pet extends Identifiable implements RefMapping {
    @NotBlank
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "species_id")
    @Fetch(FetchMode.JOIN)
    @JsonIgnore
    @NotNull
    private Species species;

    @NotNull
    @Min(value = 1)
    private Double price;

    @Transient
    private String speciesEnc;

    @JsonGetter("species")
    public String getSpeciesEnc() {
        if (species != null)
            speciesEnc = toRefMapping(species.getId(), species.getName());
        return speciesEnc;
    }

    @JsonSetter("species")
    public void setSpeciesEnc(String speciesEnc) {
        if (speciesEnc != null) {
            species = new Species();
            species.setId(fromRefMapping(speciesEnc));
        }
        this.speciesEnc = speciesEnc;
    }
}
