package com.net128.oss.web.lib.jpa.csv.pet.model;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "APP_USER")
public class AppUser extends Identifiable {
    @NotBlank
    private String name;
    @NotBlank
    private String password;
    @NotBlank
    private String email;
}
