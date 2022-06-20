package com.example.restfulapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DbDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_id", nullable = false)
    private String fieldId;

    @Column(name = "type", nullable = false)
    private String type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "db_user")
    private DbUser dbUser;
}
