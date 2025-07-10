package com.tsc.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tsc.model.Entry;

public interface EntryRepository extends JpaRepository<Entry, Long> {

	@Query("SELECT e.gate, COUNT(e) FROM Entry e WHERE e.timestamp >= :startOfDay AND e.timestamp < :endOfDay GROUP BY e.gate")
	List<Object[]> countByGateForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	@Query("SELECT COUNT(e) FROM Entry e WHERE e.gate = :gate AND e.timestamp >= :startOfDay AND e.timestamp < :endOfDay")
	Long countByGateForDay(@Param("gate") String gate, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}