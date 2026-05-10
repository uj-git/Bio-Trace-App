package com.biotrace.repository;

import com.biotrace.model.ScanRecord;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository gives us all the database operations for free —
// save(), findById(), findAll(), deleteById() etc.
// We just declare the interface, Spring creates the implementation automatically.
//
// First type = our entity class (ScanRecord)
// Second type = the type of our primary key (Long, because id is a Long)
public interface ScanRecordRepository extends JpaRepository<ScanRecord, Long> {
    // No code needed here for our 3 basic endpoints.
    // JpaRepository already provides everything we need.
}
