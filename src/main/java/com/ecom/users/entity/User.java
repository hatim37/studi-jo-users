package com.ecom.users.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "\"user\"")
public class User  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String name;
    private String username;
    @Column(unique = true)
    private String email;
    @Column(name = "password")
    private String password;
    private Boolean active = false;
    private String secretKey;
    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = {@JoinColumn(name = "user_id",referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id",referencedColumnName = "role_id")}
    )
    private Set<Role> roles = new HashSet<>();

}
