package com.ecom.users.repository;

import com.ecom.users.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Integer> {

    Optional<Role> findByLibelle(String libelle);

}
