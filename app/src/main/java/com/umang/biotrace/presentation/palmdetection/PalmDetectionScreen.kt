package com.umang.biotrace.presentation.palmdetection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PalmDetectionScreen(
    onPalmCaptured: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Camera Preview Placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        // Palm Overlay Placeholder
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Palm Overlay Here",
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {

            Text(
                text = "Light: Normal",
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPalmCaptured
            ) {
                Text(text = "Capture Palm")
            }
        }
    }
}