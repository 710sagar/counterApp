package com.tsc.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
		@Index(name = "idx_exit_lot", columnList = "lot"),
		@Index(name = "idx_exit_timestamp", columnList = "timestamp"),
		@Index(name = "idx_exit_lot_timestamp", columnList = "lot,timestamp")
})
public class ExitRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String lot;
	private LocalDateTime timestamp;

	public ExitRecord() {
		super();
	}

	public ExitRecord(String lot) {
		super();
		this.lot = lot;
		// LocalDateTime.now() will use the system timezone (Toronto after our config)
		this.timestamp = LocalDateTime.now();
	}

	// PrePersist hook to ensure timestamp is set
	@PrePersist
	public void prePersist() {
		if (this.timestamp == null) {
			this.timestamp = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getLot() {
		return lot;
	}
	
	public void setLot(String lot) {
		this.lot = lot;
	}
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
}