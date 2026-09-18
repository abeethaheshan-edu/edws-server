package com.edws.gov.repo;

import com.edws.gov.entity.GnDivision;
import com.edws.gov.entity.User;
import com.edws.gov.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByProfile_Nic(String nic);

    Optional<User> findByProfile_Nic(String nic);

    Page<User> findByGnDivisionAndRole(GnDivision gnDivision, Role role, Pageable pageable);

    Optional<User> findByIdAndRole(String id, Role role);

    long countByGnDivisionAndRole(GnDivision gnDivision, Role role);
}
