package com.pyamsoft.tickertape.alert.ui.components

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
@CheckResult
internal fun rememberContentDescription(
    isChecked: Boolean,
    contentDescription: String?,
    contentDescriptionOn: String?,
    contentDescriptionOff: String?
): String? =
    remember(
        isChecked,
        contentDescription,
        contentDescriptionOn,
        contentDescriptionOff,
    ) {
        // If all null, fast path
        if (contentDescription == null &&
            contentDescriptionOff == null &&
            contentDescriptionOn == null
        ) {
            return@remember null
        }

        // If specifics are null, just return general
        if (contentDescriptionOff == null && contentDescriptionOn == null) {
            return@remember contentDescription
        }

        return@remember if (isChecked) {
            contentDescriptionOn ?: contentDescription
        } else {
            contentDescriptionOff ?: contentDescription
        }
    }