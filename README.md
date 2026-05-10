# BioTrace — Palm & Finger Detection

Native Android app for biometric palm and finger capture with real-time hand detection.

## Tech Stack
- **Android** — Kotlin, Jetpack Compose, MVVM, Koin, CameraX, MediaPipe
- **Backend** — Java Spring Boot, H2 in-memory database

## Running the App

1. Clone and open in Android Studio
2. Sync Gradle and run on a **physical device** (camera required)
3. Grant camera and storage permissions on first launch

## Running the Backend

```bash
cd backend
./mvnw spring-boot:run       # Mac/Linux
mvnw.cmd spring-boot:run     # Windows
```
Server: `http://localhost:8080`  
H2 Console: `http://localhost:8080/h2-console` — URL: `jdbc:h2:mem:biotrace`, user: `sa`, pass: *(blank)*

## Connecting Android to Backend

In `di/networkModule.kt` update `BASE_URL`:
- **Emulator:** `http://10.0.2.2:8080/`
- **Physical device:** `http://<YOUR_PC_IP>:8080/` *(same WiFi required)*

Find your IP: `ipconfig` on Windows, `ifconfig` on Mac

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/scans` | Upload scan session |
| GET | `/api/scans` | List all scans |
| GET | `/api/scans/{id}` | Get scan by ID |

## Key Features
- Left/right hand detection, dorsal side detection
- Blur detection before saving finger images
- Finger validation against palm minutiae records
- Images saved to `Finger Data` folder with timestamped filenames
- Camera metrics saved per device ID
- Auto-uploads completed session to backend
