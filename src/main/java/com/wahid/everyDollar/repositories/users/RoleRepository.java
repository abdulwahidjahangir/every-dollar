package com.wahid.everyDollar.repositories.users;

import com.wahid.everyDollar.models.users.AppRole;
import com.wahid.everyDollar.models.users.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
