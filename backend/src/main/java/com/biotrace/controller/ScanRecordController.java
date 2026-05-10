package com.biotrace.controller;

import com.biotrace.model.ScanRecord;
import com.biotrace.model.ScanRequest;
import com.biotrace.service.ScanRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = this class handles HTTP requests and returns JSON responses
// @RequestMapping = all endpoints in this class start with /api/scans
@RestController
@RequestMapping("/api/scans")
public class ScanRecordController {

    private final ScanRecordService service;

    public ScanRecordController(ScanRecordService service) {
        this.service = service;
    }

    // ── POST /api/scans ───────────────────────────────────────────────────────
    // Android app calls this after finishing all 5 finger scans.
    // It sends a JSON body, we save it, and return the saved record with its new id.
    //
    // @RequestBody = read the JSON from the request body and convert to ScanRequest
    // @Valid = run the validation rules (@NotBlank etc) on the incoming data
    // ResponseEntity = lets us control the HTTP status code (201 Created)
    @PostMapping
    public ResponseEntity<ScanRecord> create(@Valid @RequestBody ScanRequest request) {
        ScanRecord saved = service.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── GET /api/scans ────────────────────────────────────────────────────────
    // Returns a JSON array of all scan records.
    // e.g. [{ "id": 1, "handSide": "Left Hand", ... }, { "id": 2, ... }]
    @GetMapping
    public ResponseEntity<List<ScanRecord>> getAll() {
        List<ScanRecord> records = service.findAll();
        return ResponseEntity.ok(records);
    }

    // ── GET /api/scans/{id} ───────────────────────────────────────────────────
    // Returns the details of one scan record by its id.
    // {id} in the URL is a path variable, e.g. GET /api/scans/3
    //
    // If found  → 200 OK with the record
    // If not found → 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<ScanRecord> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
