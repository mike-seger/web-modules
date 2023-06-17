package com.net128.oss.web.lib.jpa.csv.data.test;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class W extends Y {

    @Column
    private String wattr1;
    
    @Column
    private String wattr2;

}