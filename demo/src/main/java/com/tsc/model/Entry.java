package com.tsc.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Entry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String gate;

	@Enumerated(EnumType.STRING)
	private EntryType entryType;

	private LocalDateTime timestamp;

	private String createdBy; // Track which user created the entry

	private boolean isDeleted = false; // Soft delete flag

	private String deletedBy; // Track who deleted the entry

	private LocalDateTime deletedAt; // When it was deleted

	// Toronto timezone
	private static final ZoneId TORONTO_ZONE = ZoneId.of("America/Toronto");

	// Constructors
	public Entry() {
		super();
	}

	public Entry(String gate, EntryType entryType, LocalDateTime timestamp, String createdBy) {
		super();
		this.gate = gate;
		this.entryType = entryType;
		// Store the timestamp as provided (should already be Toronto time from controller)
		this.timestamp = timestamp;
		this.createdBy = createdBy;
	}

	// PrePersist hook - only set if null
	@PrePersist
	public void prePersist() {
		if (this.timestamp == null) {
			// Get current Toronto time regardless of server timezone
			this.timestamp = ZonedDateTime.now(TORONTO_ZONE).toLocalDateTime();
		}
	}

	// PreUpdate hook for soft deletes
	@PreUpdate
	public void preUpdate() {
		if (this.isDeleted && this.deletedAt == null) {
			// Get current Toronto time regardless of server timezone
			this.deletedAt = ZonedDateTime.now(TORONTO_ZONE).toLocalDateTime();
		}
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGate() {
		return gate;
	}

	public void setGate(String gate) {
		this.gate = gate;
	}

	public EntryType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryType entryType) {
		this.entryType = entryType;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
		if (deleted && this.deletedAt == null) {
			// Get current Toronto time regardless of server timezone
			this.deletedAt = ZonedDateTime.now(TORONTO_ZONE).toLocalDateTime();
		}
	}

	public String getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	@Override
	public String toString() {
		return "Entry [id=" + id + ", gate=" + gate + ", entryType=" + entryType +
				", timestamp=" + timestamp + ", createdBy=" + createdBy +
				", isDeleted=" + isDeleted + "]";
	}
}