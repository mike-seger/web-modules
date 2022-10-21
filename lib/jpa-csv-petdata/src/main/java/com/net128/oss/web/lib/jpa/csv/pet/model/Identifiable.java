package com.net128.oss.web.lib.jpa.csv.pet.model;

import com.net128.oss.web.lib.jpa.csv.pet.repo.EntityChangeBroadcaster;
import lombok.Data;

import javax.persistence.*;

@MappedSuperclass
@Data
@EntityListeners(EntityChangeBroadcaster.class)
public abstract class Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
