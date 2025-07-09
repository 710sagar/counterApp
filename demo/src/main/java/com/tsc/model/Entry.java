package com.tsc.model;

import java.time.LocalDateTime;
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

	// Constructors
	public Entry() {
		super();
	}

	public Entry(String gate, EntryType entryType, LocalDateTime timestamp, String createdBy) {
		super();
		this.gate = gate;
		this.entryType = entryType;
		this.timestamp = timestamp;
		this.createdBy = createdBy;
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