package com.biotrace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

// @Entity tells Spring: "create a database table for this class"
// Each field becomes a column, each object becomes a row.
@Entity
@Table(name = "scan_records")
public class ScanRecord {

    // @Id = primary key. @GeneratedValue = auto-increment (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which hand was scanned: "Left Hand" or "Right Hand"
    @NotBlank
    @Column(nullable = false)
    private String handSide;

    // File path of the saved palm image on the device
    @Column
    private String palmImagePath;

    // We store the 5 finger image paths as a comma-separated string.
    // e.g. "/storage/FingerData/Left_Thumb_20250509.jpg,..."
    // Simple approach — no need for a separate table.
    @Column(length = 1000)
    private String fingerImagePaths;

    // Camera metrics saved as simple fields
    @Column
    private Float brightnessScore;

    @Column
    private Float blurScore;

    @Column
    private Float focusDistance;

    @Column
    private String lightType;       // "Normal light", "Low light", "Bright light"

    @Column
    private String deviceId;

    // Automatically set when the record is first saved
    @Column(nullable = false)
    private LocalDateTime capturedAt;

    // @PrePersist runs automatically just before saving to the database
    @PrePersist
    public void prePersist() {
        this.capturedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    // Spring needs these to read and write the fields.

    public Long getId() { return id; }

    public String getHandSide() { return handSide; }
    public void setHandSide(String handSide) { this.handSide = handSide; }

    public String getPalmImagePath() { return palmImagePath; }
    public void setPalmImagePath(String palmImagePath) { this.palmImagePath = palmImagePath; }

    public String getFingerImagePaths() { return fingerImagePaths; }
    public void setFingerImagePaths(String fingerImagePaths) { this.fingerImagePaths = fingerImagePaths; }

    public Float getBrightnessScore() { return brightnessScore; }
    public void setBrightnessScore(Float brightnessScore) { this.brightnessScore = brightnessScore; }

    public Float getBlurScore() { return blurScore; }
    public void setBlurScore(Float blurScore) { this.blurScore = blurScore; }

    public Float getFocusDistance() { return focusDistance; }
    public void setFocusDistance(Float focusDistance) { this.focusDistance = focusDistance; }

    public String getLightType() { return lightType; }
    public void setLightType(String lightType) { this.lightType = lightType; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public LocalDateTime getCapturedAt() { return capturedAt; }
}
