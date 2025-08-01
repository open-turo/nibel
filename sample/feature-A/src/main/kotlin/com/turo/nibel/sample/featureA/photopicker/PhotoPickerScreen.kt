package com.turo.nibel.sample.featureA.photopicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turo.nibel.sample.common.TopAppBar
import com.turo.nibel.sample.navigation.PhotoPickerArgs
import nibel.annotations.ImplementationType
import nibel.annotations.UiEntry
import nibel.runtime.LocalArgs
import nibel.runtime.LocalNavigationController

// Sample photo data for demonstration
private val samplePhotos = listOf(
    PhotoPickerResult("https://example.com/photo1.jpg", "Beach Sunset", System.currentTimeMillis() - 10000),
    PhotoPickerResult("https://example.com/photo2.jpg", "Mountain View", System.currentTimeMillis() - 20000),
    PhotoPickerResult("https://example.com/photo3.jpg", "City Lights", System.currentTimeMillis() - 30000),
    PhotoPickerResult("https://example.com/photo4.jpg", "Forest Path", System.currentTimeMillis() - 40000),
    PhotoPickerResult("https://example.com/photo5.jpg", "Ocean Waves", System.currentTimeMillis() - 50000),
)

/**
 * Demonstration screen that shows how to create a screen that returns a result.
 * This screen simulates a photo picker that allows users to select a photo
 * and return it to the calling screen.
 */
@UiEntry(
    type = ImplementationType.Composable,
    result = PhotoPickerResult::class,
    args = PhotoPickerArgs::class
)
@Composable
fun PhotoPickerScreen() {
    val navigator = LocalNavigationController.current
    val args = LocalArgs.current as PhotoPickerArgs
    var selectedPhoto by remember { mutableStateOf<PhotoPickerResult?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = "Pick a Photo",
            onBack = {
                // Cancel and go back without result
                navigator.cancelResultAndNavigateBack()
            }
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Choose a photo to return to the previous screen:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Max photos allowed: ${args.maxPhotos}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Multiple selection: ${if (args.allowMultiple) "Yes" else "No"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(samplePhotos) { photo ->
                    PhotoItem(
                        photo = photo,
                        isSelected = selectedPhoto == photo,
                        onPhotoClick = { selectedPhoto = photo }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        navigator.cancelResultAndNavigateBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        selectedPhoto?.let { photo ->
                            navigator.setResultAndNavigateBack(photo)
                        }
                    },
                    enabled = selectedPhoto != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Select Photo")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoItem(
    photo: PhotoPickerResult,
    isSelected: Boolean,
    onPhotoClick: () -> Unit
) {
    Card(
        onClick = onPhotoClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = photo.photoName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = photo.photoUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
