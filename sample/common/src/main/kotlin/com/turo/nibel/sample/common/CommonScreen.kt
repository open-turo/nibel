package com.turo.nibel.sample.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nibel.annotations.ImplementationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <NB : NextButton> CommonScreen(
    title: String,
    inputText: String,
    implementationType: ImplementationType,
    nextButtons: List<NB>,
    onBack: () -> Unit,
    onContinue: (NB) -> Unit,
    onInputTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    inputTextEditable: Boolean = true,
    inputTextVisible: Boolean = true,
    additionalContent: @Composable (() -> Unit)? = null,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = title,
                onBack = onBack,
            )
        },
        content = { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                CommonScreenContent(
                    inputText = inputText,
                    implementationType = implementationType,
                    nextButtons = nextButtons,
                    inputTextEditable = inputTextEditable,
                    inputTextVisible = inputTextVisible,
                    onContinue = onContinue,
                    onInputTextChanged = onInputTextChanged,
                    additionalContent = additionalContent,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <NB : NextButton> CommonScreenContent(
    inputText: String,
    implementationType: ImplementationType,
    nextButtons: List<NB>,
    inputTextEditable: Boolean,
    inputTextVisible: Boolean,
    onContinue: (NB) -> Unit,
    onInputTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    additionalContent: @Composable (() -> Unit)? = null,
) {
    val supportingText =
        if (inputTextEditable) {
            stringResource(R.string.input_supporting_text)
        } else {
            stringResource(R.string.input_read_only_text)
        }

    LazyColumn(modifier, contentPadding = PaddingValues(16.dp)) {
        item {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Jetpack Compose screen",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Implementation Type: $implementationType",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(64.dp))

            if (inputTextVisible) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    maxLines = 1,
                    enabled = inputTextEditable,
                    label = {
                        Text("Input text")
                    },
                    supportingText = {
                        Text(text = supportingText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Display additional content if provided
            additionalContent?.invoke()
        }
        items(nextButtons.size) {
            Button(
                onClick = { onContinue(nextButtons[it]) },
                Modifier
                    .height(55.dp)
                    .fillMaxWidth(),
                shape = TextFieldDefaults.outlinedShape,
            ) {
                Text(text = nextButtons[it].title)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
fun Preview_CommonScreen() {
    CommonScreen(
        title = "Preview",
        inputText = "123",
        implementationType = ImplementationType.Composable,
        nextButtons = listOf(
            GenericNextButton("Button 1"),
            GenericNextButton("Button 2"),
        ),
        onBack = {},
        onContinue = {},
        onInputTextChanged = {},
        modifier = Modifier,
    )
}
