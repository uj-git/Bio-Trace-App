package com.umang.biotrace.presentation.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onPalmScanClick: () -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    // Refresh from server every time HomeScreen becomes visible
    LaunchedEffect(Unit) {
        viewModel.refreshFromServer()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "BioTrace",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Palm & Finger Scanner",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Last scan card ────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.lastScan != null) {
            LastScanCard(scan = state.lastScan!!)
        } else {
            NoScanCard()
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onPalmScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Start New Scan", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LastScanCard(scan: com.umang.biotrace.data.LastScanSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last Scan",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = "ID #${scan.id}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ScanRow(label = "Hand",        value = scan.handSide)
        ScanRow(label = "Fingers",     value = "${scan.fingerCount}/5")
        ScanRow(label = "Blur Score",  value = scan.blurScore.format())
        ScanRow(label = "Brightness",  value = scan.brightnessScore.format())
        ScanRow(label = "Focus Dist.", value = scan.focusDistance.format())
        ScanRow(label = "Light",       value = scan.lightType)

        if (scan.capturedAt.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Captured: ${scan.capturedAt.take(19).replace("T", "  ")}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun NoScanCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No scans yet.\nTap Start New Scan to begin.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ScanRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun Float.format() = String.format("%.2f", this)