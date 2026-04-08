package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Preferences extends Identifiable {
    @NotNull
    private Boolean darkMode;
    
    private String currentTab;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @NotNull
    @Props.RefMapping(labelField = "name")
    private AppUser appUser;

    public void copy(Preferences preferences) {
        this.setDarkMode(preferences.getDarkMode());
    }
}
