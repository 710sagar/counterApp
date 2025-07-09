package com.tsc.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.tsc.model.Entry;
import com.tsc.model.EntryType;

public interface EntryRepository extends JpaRepository<Entry, Long> {

	// Get counts by gate and entry type for a specific day (excluding deleted entries)
	@Query("SELECT e.gate, e.entryType, COUNT(e) FROM Entry e WHERE e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false GROUP BY e.gate, e.entryType")
	List<Object[]> countByGateAndTypeForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	// Get count for specific gate and entry type for a day
	@Query("SELECT COUNT(e) FROM Entry e WHERE e.gate = :gate AND e.entryType = :entryType AND e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false")
	Long countByGateAndTypeForDay(@Param("gate") String gate, @Param("entryType") EntryType entryType, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	// Get total count for a gate (all entry types) for a day
	@Query("SELECT COUNT(e) FROM Entry e WHERE e.gate = :gate AND e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false")
	Long countByGateForDay(@Param("gate") String gate, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	// Get all counts by gate for a day (all entry types combined)
	@Query("SELECT e.gate, COUNT(e) FROM Entry e WHERE e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false GROUP BY e.gate")
	List<Object[]> countByGateForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	// Find last entry by user for a specific day (for undo functionality)
	@Query("SELECT e FROM Entry e WHERE e.createdBy = :username AND e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false ORDER BY e.timestamp DESC")
	List<Entry> findLastEntryByUserForDay(@Param("username") String username, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	// Soft delete entries for a day (mark as deleted instead of actually deleting)
	@Modifying
	@Transactional
	@Query("UPDATE Entry e SET e.isDeleted = true, e.deletedBy = :deletedBy, e.deletedAt = :deletedAt WHERE e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false")
	int softDeleteEntriesForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("deletedBy") String deletedBy, @Param("deletedAt") LocalDateTime deletedAt);

	// Find all non-deleted entries for a day
	@Query("SELECT e FROM Entry e WHERE e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false ORDER BY e.timestamp DESC")
	List<Entry> findNonDeletedEntriesForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	// Find entries by gate for a day (non-deleted)
	@Query("SELECT e FROM Entry e WHERE e.gate = :gate AND e.timestamp >= :startOfDay AND e.timestamp < :endOfDay AND e.isDeleted = false ORDER BY e.timestamp DESC")
	List<Entry> findEntriesByGateForDay(@Param("gate") String gate, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}