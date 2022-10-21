package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.net128.oss.web.lib.jpa.csv.util.RefMapping;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends Identifiable implements RefMapping {
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
    @JsonIgnore
    private Country country;
    @NotBlank
    @Pattern(regexp = "^\\+\\d{1,6}\\s\\d{1,14}(\\s\\d{1,13})?",
        message = "Required E164 format!\nValid examples:\n+1 1234 567890\n+44 1234567890")
    private String phone;

    @Transient
    private String countryEnc;

    @JsonGetter("country")
    public String getCountryEnc() {
        if (country != null)
            countryEnc = toRefMapping(country.getId(), country.getName());
        return countryEnc;
    }

    @JsonSetter("country")
    public void setCountryEnc(String countryEnc) {
        if (countryEnc != null) {
            country = new Country();
            country.setId(fromRefMapping(countryEnc));
        }
        this.countryEnc = countryEnc;
    }
}
