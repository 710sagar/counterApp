package com.tsc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tsc.model.ExitRecord;

public interface ExitRecordInterface extends JpaRepository<ExitRecord, Long> {

}
