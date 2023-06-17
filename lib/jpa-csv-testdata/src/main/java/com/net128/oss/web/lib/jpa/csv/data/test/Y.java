package com.net128.oss.web.lib.jpa.csv.data.test;

import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@Props.Hidden
public class Y {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Props.Hidden
    private Long id;

    @Column
    private String yattr1;
    
    @Column
    private String yattr2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Y)) return false;
        return Objects.equals(id, ((Y)o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}