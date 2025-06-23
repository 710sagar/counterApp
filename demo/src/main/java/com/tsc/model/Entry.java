package com.tsc.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Entry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    private String gate;
    private LocalDateTime timestamp;
    
    
	public Entry() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Entry(String gate, LocalDateTime timestamp) {
		super();

		this.gate = gate;
		this.timestamp = timestamp;
	}
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
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "Entry [id=" + id + ", gate=" + gate + ", timestamp=" + timestamp + "]";
	}
    
    
}
