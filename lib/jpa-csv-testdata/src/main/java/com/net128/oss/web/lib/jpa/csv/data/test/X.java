package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class X extends Y {

    @Column
    private String xattr1;
    
    @Column
    private String xattr2;

}