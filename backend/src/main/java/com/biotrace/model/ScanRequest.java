package com.biotrace.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

// DTO = Data Transfer Object.
// This is the shape of the JSON body the Android app sends in the POST request.
// It's separate from ScanRecord so we control exactly what the API accepts.
public class ScanRequest {

    @NotBlank(message = "handSide is required")
    private String handSide;

    private String palmImagePath;

    // List of 5 finger image paths from the Android device
    private List<String> fingerImagePaths;

    private Float brightnessScore;
    private Float blurScore;
    private Float focusDistance;
    private String lightType;
    private String deviceId;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getHandSide() { return handSide; }
    public void setHandSide(String handSide) { this.handSide = handSide; }

    public String getPalmImagePath() { return palmImagePath; }
    public void setPalmImagePath(String palmImagePath) { this.palmImagePath = palmImagePath; }

    public List<String> getFingerImagePaths() { return fingerImagePaths; }
    public void setFingerImagePaths(List<String> fingerImagePaths) { this.fingerImagePaths = fingerImagePaths; }

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
}
