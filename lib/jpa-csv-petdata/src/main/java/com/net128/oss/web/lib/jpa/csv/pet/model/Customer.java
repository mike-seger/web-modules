package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends Identifiable {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String address;
    @NotBlank
    private String zipCode;
    @NotBlank
    private String city;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @Props.RefMapping(labelField = "name")
    private Country country;
    @NotBlank
    @Pattern(regexp = "^\\+\\d{1,6}\\s\\d{1,14}(\\s\\d{1,13})?",
            message = "Required E164 format!\nValid examples:\n+1 1234 567890\n+44 1234567890")
    private String phone;
}
