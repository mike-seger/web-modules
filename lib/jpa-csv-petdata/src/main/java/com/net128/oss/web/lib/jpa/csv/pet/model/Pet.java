package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.net128.oss.web.lib.jpa.csv.util.Props;
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
public class Pet extends Identifiable {
    @NotBlank
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "species_id")
    @Fetch(FetchMode.JOIN)
    @NotNull
    @Props.RefMapping(labelField = "name")
    private Species species;

    @NotNull
    @Min(value = 1)
    private Double price;

    @Transient
    private String speciesEnc;
}
