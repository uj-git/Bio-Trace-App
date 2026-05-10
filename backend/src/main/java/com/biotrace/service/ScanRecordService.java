package com.biotrace.service;

import com.biotrace.model.ScanRecord;
import com.biotrace.model.ScanRequest;
import com.biotrace.repository.ScanRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// @Service marks this as the "business logic" layer.
// The Controller receives the HTTP request, then calls this Service.
// The Service does the work, then calls the Repository to save/fetch data.
//
// Controller  →  Service  →  Repository  →  Database
@Service
public class ScanRecordService {

    // Spring automatically injects the repository here (this is called Dependency Injection)
    private final ScanRecordRepository repository;

    public ScanRecordService(ScanRecordRepository repository) {
        this.repository = repository;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    // Converts the incoming request into a ScanRecord and saves it
    public ScanRecord save(ScanRequest request) {
        ScanRecord record = new ScanRecord();

        record.setHandSide(request.getHandSide());
        record.setPalmImagePath(request.getPalmImagePath());
        record.setBrightnessScore(request.getBrightnessScore());
        record.setBlurScore(request.getBlurScore());
        record.setFocusDistance(request.getFocusDistance());
        record.setLightType(request.getLightType());
        record.setDeviceId(request.getDeviceId());

        // Convert the List of finger paths into a single comma-separated string for storage
        // e.g. ["path1", "path2"] → "path1,path2"
        if (request.getFingerImagePaths() != null) {
            record.setFingerImagePaths(String.join(",", request.getFingerImagePaths()));
        }

        return repository.save(record);
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────
    public List<ScanRecord> findAll() {
        return repository.findAll();
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────
    // Optional means it might be empty if no record with that id exists
    public Optional<ScanRecord> findById(Long id) {
        return repository.findById(id);
    }
}
