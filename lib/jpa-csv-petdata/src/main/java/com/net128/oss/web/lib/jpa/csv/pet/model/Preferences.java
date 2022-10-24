package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

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
