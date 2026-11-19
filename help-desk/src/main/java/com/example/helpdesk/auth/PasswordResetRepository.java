package com.example.helpdesk.auth;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<PasswordReset> findByTokenHash(String hash);

  void deleteByUserId(Long userId);
}
