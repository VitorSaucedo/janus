package com.vitorsaucedo.janus.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findByEmailOrderByCreatedAtDesc(String email);
}
