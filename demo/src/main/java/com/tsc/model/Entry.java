package com.tsc.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import jakarta.persistence.*;

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
		// Convert the timestamp to Toronto timezone
		this.timestamp = convertToTorontoTime(timestamp);
		this.createdBy = createdBy;
	}

	// PrePersist hook to ensure timestamp is always in Toronto timezone
	@PrePersist
	public void prePersist() {
		if (this.timestamp == null) {
			this.timestamp = getCurrentTorontoTime();
		}
	}

	// PreUpdate hook for soft deletes
	@PreUpdate
	public void preUpdate() {
		if (this.isDeleted && this.deletedAt == null) {
			this.deletedAt = getCurrentTorontoTime();
		}
	}

	// Helper method to get current Toronto time
	private static LocalDateTime getCurrentTorontoTime() {
		return ZonedDateTime.now(TORONTO_ZONE).toLocalDateTime();
	}

	// Helper method to convert any LocalDateTime to Toronto timezone
	private static LocalDateTime convertToTorontoTime(LocalDateTime dateTime) {
		// If the input is already assumed to be in Toronto time, return as is
		// Otherwise, convert from system default to Toronto
		if (dateTime == null) {
			return getCurrentTorontoTime();
		}
		// Assume the input LocalDateTime is in system default timezone
		ZonedDateTime systemZoned = dateTime.atZone(ZoneId.systemDefault());
		// Convert to Toronto timezone
		ZonedDateTime torontoZoned = systemZoned.withZoneSameInstant(TORONTO_ZONE);
		return torontoZoned.toLocalDateTime();
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
			this.deletedAt = getCurrentTorontoTime();
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