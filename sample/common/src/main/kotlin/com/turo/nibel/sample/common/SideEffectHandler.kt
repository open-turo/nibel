package com.turo.nibel.sample.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.turo.nibel.runtime.LocalNavigationController
import com.turo.nibel.runtime.NavigationController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <SE> SideEffectHandler(
    sideEffects: Flow<SE>,
    key: Any = Unit,
    handle: NavigationController.(SE) -> Unit,
) {
    val navigationController = LocalNavigationController.current
    LaunchedEffect(key1 = key) {
        sideEffects.collectLatest { navigationController.handle(it) }
    }
}
