package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "city")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class City implements Serializable {

    private static final long serialVersionUID = 671754347547L;

    @Id
    private UUID id;

    @Column(name = "name", length = 30)
    private String name;

}
