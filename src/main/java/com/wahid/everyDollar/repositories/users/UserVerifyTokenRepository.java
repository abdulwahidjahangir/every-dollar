package com.wahid.everyDollar.repositories.users;

import com.wahid.everyDollar.models.users.UserVerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVerifyTokenRepository extends JpaRepository<UserVerifyToken, Long> {
    Optional<UserVerifyToken> findByToken(String token);
}
