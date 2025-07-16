package com.tsc.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
		// TODO Auto-generated constructor stub
	}
	

	public ExitRecord(String lot) {
		super();
		this.lot = lot;
		this.timestamp = LocalDateTime.now();
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
