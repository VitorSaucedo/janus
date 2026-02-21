package com.vitorsaucedo.janus.domain.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findLastByUser(User user, Pageable pageable);
}
