package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.net128.oss.web.lib.jpa.csv.util.RefMapping;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@ToString
@Getter @Setter
@Table(uniqueConstraints= {
    //FIXME This doesn't kick in with H2!!
    @UniqueConstraint(columnNames = {Purchase.petID})
})
public class Purchase extends Identifiable implements RefMapping {
    final static String petID = "pet_id";
    @NotNull
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name=petID)
    @JsonIgnore
    @NotNull
    private Pet pet;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JsonIgnore
    @NotNull
    private Customer customer;

    @PrePersist
    public void prePersist() {
        date = LocalDateTime.now();
    }
    
    @Transient
    private String petEnc;

    @JsonGetter("pet")
    public String getPetEnc() {
        if (pet != null)
            petEnc = toRefMapping(pet.getId(), pet.getName());
        return petEnc;
    }

    @JsonSetter("pet")
    public void setPetEnc(String petEnc) {
        if (petEnc != null) {
            pet = new Pet();
            pet.setId(fromRefMapping(petEnc));
        }
        this.petEnc = petEnc;
    }

    @Transient
    private String customerEnc;

    @JsonGetter("customer")
    public String getCustomerEnc() {
        if (customer != null)
            customerEnc = toRefMapping(customer.getId(),
                customer.getFirstName()+" "+customer.getLastName());
        return customerEnc;
    }

    @JsonSetter("customer")
    public void setCustomerEnc(String customerEnc) {
        if (customerEnc != null) {
            customer = new Customer();
            customer.setId(fromRefMapping(customerEnc));
        }
        this.customerEnc = customerEnc;
    }
}
