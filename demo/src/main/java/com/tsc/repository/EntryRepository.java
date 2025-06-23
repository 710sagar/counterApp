package com.tsc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tsc.model.Entry;

public interface EntryRepository extends JpaRepository<Entry, Long>{

	    @Query("SELECT e.gate, COUNT(e) FROM Entry e GROUP BY e.gate")
	    List<Object[]> countByGate();
	

}
