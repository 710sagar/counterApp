package com.tsc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tsc.model.ExitRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface ExitRecordInterface extends JpaRepository<ExitRecord, Long> {

    // Count exits by lot for today
    @Query("SELECT COUNT(e) FROM ExitRecord e WHERE e.lot = :lot AND e.timestamp >= :startOfDay AND e.timestamp < :endOfDay")
    Long countByLotForDay(@Param("lot") String lot, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Get all counts by lot for today (for admin)
    @Query("SELECT e.lot, COUNT(e) FROM ExitRecord e WHERE e.timestamp >= :startOfDay AND e.timestamp < :endOfDay GROUP BY e.lot")
    List<Object[]> countAllByLotForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Find exits for a specific day (if needed for logs)
    @Query("SELECT e FROM ExitRecord e WHERE e.timestamp >= :startOfDay AND e.timestamp < :endOfDay ORDER BY e.timestamp DESC")
    List<ExitRecord> findExitsForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}