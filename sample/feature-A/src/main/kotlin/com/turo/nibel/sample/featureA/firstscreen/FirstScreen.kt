package com.turo.nibel.sample.featureA.firstscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turo.nibel.sample.common.CommonScreen
import com.turo.nibel.sample.common.SideEffectHandler
import com.turo.nibel.sample.featureA.photopicker.PhotoPickerResult
import com.turo.nibel.sample.featureA.photopicker.PhotoPickerScreenEntry
import com.turo.nibel.sample.featureA.secondscreen.SecondArgs
import com.turo.nibel.sample.featureA.secondscreen.SecondScreenEntry
import com.turo.nibel.sample.navigation.FirstScreenDestination
import com.turo.nibel.sample.navigation.PhotoPickerArgs
import kotlinx.coroutines.flow.Flow
import nibel.annotations.ImplementationType
import nibel.annotations.UiExternalEntry
import nibel.runtime.LocalImplementationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@UiExternalEntry(
    type = ImplementationType.Fragment,
    destination = FirstScreenDestination::class,
)
@Composable
fun FirstScreen(viewModel: FirstViewModel = hiltViewModel()) {
    SideEffectHandler(viewModel.sideEffects, viewModel)

    val state by viewModel.state.collectAsStateWithLifecycle()

    CommonScreen(
        title = state.title,
        inputText = state.inputText,
        implementationType = LocalImplementationType.current!!,
        onBack = viewModel::onBack,
        nextButtons = state.nextButtons,
        onContinue = viewModel::onContinue,
        onInputTextChanged = viewModel::onInputTextChanged,
        additionalContent = {
            state.selectedPhoto?.let { photo ->
                PhotoResultDisplay(photo)
            }
        }
    )
}

@Composable
private fun SideEffectHandler(sideEffects: Flow<FirstSideEffect>, viewModel: FirstViewModel) {
    SideEffectHandler(sideEffects) {
        when (it) {
            is FirstSideEffect.NavigateBack -> navigateBack()
            is FirstSideEffect.NavigateToSecondScreen -> {
                val args = SecondArgs(it.inputText)
                navigateTo(SecondScreenEntry.newInstance(args))
            }
            is FirstSideEffect.NavigateToPhotoPicker -> {
                val args = PhotoPickerArgs(allowMultiple = false, maxPhotos = 1)
                navigateForResult(
                    entry = PhotoPickerScreenEntry.newInstance(args),
                    callback = { result: PhotoPickerResult? ->
                        viewModel.onPhotoSelected(result)
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoResultDisplay(photo: PhotoPickerResult) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Selected Photo Result:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Name: ${photo.photoName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "URL: ${photo.photoUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Selected: ${dateFormatter.format(Date(photo.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
