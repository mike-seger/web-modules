package com.net128.oss.web.lib.jpa.csv.data.test;

import com.net128.oss.web.lib.jpa.csv.Identifiable;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.*;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Props.Sortable
@Props.Hidden
public class Y extends Identifiable {
    @Column
    private String yattr1;
    
    @Column
    private String yattr2;
}