package com.example.restfulapi.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DbVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_id", nullable = false)
    private String fieldId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "db_user")
    private DbUser dbUser;
}
