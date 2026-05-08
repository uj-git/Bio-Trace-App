package com.umang.biotrace.presentation.fingerdetection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FingerDetectionScreen(
    onCompleted: () -> Unit
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

        // Oval Overlay Placeholder
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(
                        color = Color.Transparent,
                        shape = CircleShape
                    )
            )

            Text(
                text = "Place Finger",
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
                text = "Finger 1/5",
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCompleted
            ) {
                Text(text = "Capture Finger")
            }
        }
    }
}