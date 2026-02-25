package com.turo.nibel.sample.featureB.fifthscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.turo.nibel.sample.navigation.FifthArgs
import com.turo.nibel.sample.navigation.FifthScreenDestination
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.LocalArgs

@UiExternalEntry(
    type = ImplementationType.Composable,
    destination = FifthScreenDestination::class,
)
@Composable
fun FifthScreen(args: FifthArgs = LocalArgs.current as FifthArgs) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Fifth Screen (feature-B)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Rendered via Nibel.ExternalContent — no Fragment wrapper",
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Label: ${args.label}",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
