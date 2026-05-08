package com.umang.biotrace.presentation.resultscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Capture Result",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Blur Score: 0.82")
            Text(text = "Brightness Score: 0.74")
            Text(text = "Focus Distance: 1.4")

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { }) {
                Text(text = "Finish")
            }
        }
    }
}