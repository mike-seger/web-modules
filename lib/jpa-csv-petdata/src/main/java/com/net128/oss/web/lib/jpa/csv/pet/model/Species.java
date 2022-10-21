package com.net128.oss.web.lib.jpa.csv.pet.model;

import lombok.*;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Species extends Identifiable {
    @NotBlank
    private String name;
}
