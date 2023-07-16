package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Table(name = "file")
public class File implements Serializable {

    private static final long serialVersionUID = 6717583475472L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "href", length = 300)
    private String href;

    @Column(name = "file_name", length = 200)
    private String fileName;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JsonIgnore
//    private User userData;

}
