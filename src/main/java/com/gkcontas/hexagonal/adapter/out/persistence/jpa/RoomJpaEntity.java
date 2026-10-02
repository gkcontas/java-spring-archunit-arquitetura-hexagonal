package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomJpaEntity {

    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private int capacity;
}
