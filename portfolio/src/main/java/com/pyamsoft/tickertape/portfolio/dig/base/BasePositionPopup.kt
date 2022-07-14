package com.pyamsoft.tickertape.portfolio.dig.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.pydroid.ui.defaults.ImageDefaults
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.ui.icon.Paid
import com.pyamsoft.tickertape.ui.icon.Tag
import com.pyamsoft.tickertape.ui.icon.Today
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import timber.log.Timber

@Composable
internal fun BasePositionPopup(
    modifier: Modifier = Modifier,
    isReadOnly: Boolean,
    isSubmitEnabled: Boolean,
    title: String,
    topFieldLabel: String,
    topFieldValue: String,
    onTopFieldChanged: (String) -> Unit,
    bottomFieldLabel: String,
    bottomFieldValue: String,
    onBottomFieldChanged: (String) -> Unit,
    dateLabel: String,
    dateField: LocalDate?,
    onDateClicked: (LocalDate?) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.Elevation,
  ) {
    Column {
      PositionToolbar(
          modifier = Modifier.fillMaxWidth(),
          title = title,
          onClose = onClose,
      )
      Surface(
          modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
        ) {
          TopField(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(bottom = MaterialTheme.keylines.baseline)
                      .focusRequester(focusRequester),
              readOnly = isReadOnly,
              label = topFieldLabel,
              value = topFieldValue,
              onChange = onTopFieldChanged,
              onNext = { focusManager.moveFocus(FocusDirection.Down) },
          )

          BottomField(
              modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
              readOnly = isReadOnly,
              label = bottomFieldLabel,
              value = bottomFieldValue,
              onChange = onBottomFieldChanged,
              onNext = { onDateClicked(dateField) },
          )

          DateField(
              modifier = Modifier.padding(MaterialTheme.keylines.baseline),
              readOnly = isReadOnly,
              contentDescription = dateLabel,
              date = dateField,
              onDateClicked = onDateClicked,
          )

          SubmitSection(
              isEnabled = isSubmitEnabled,
              onSubmit = {
                onSubmit()

                Timber.d("Re-request focus on top field")
                focusRequester.requestFocus()
              },
          )
        }
      }
    }
  }
}

@Composable
private fun SubmitSection(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onSubmit: () -> Unit,
) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(
        modifier = Modifier.weight(1F),
    )
    Button(
        onClick = onSubmit,
        enabled = isEnabled,
    ) {
      Text(
          text = "Submit",
      )
    }
  }
}

@Composable
private fun DateField(
    modifier: Modifier = Modifier,
    readOnly: Boolean,
    date: LocalDate?,
    contentDescription: String,
    onDateClicked: (LocalDate?) -> Unit
) {
  val displayDate =
      remember(date) {
        if (date == null) "--/--/----" else date.format(DateTimeFormatter.ISO_LOCAL_DATE)
      }

  Row(
      modifier =
          modifier.clickable(
              enabled = !readOnly,
          ) { onDateClicked(date) },
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
        modifier =
            Modifier.padding(end = ImageDefaults.IconSize).alpha(TextFieldDefaults.IconOpacity),
        imageVector = Icons.Filled.Today,
        contentDescription = contentDescription,
    )
    Text(
        text = displayDate,
        style = MaterialTheme.typography.body1,
    )
  }
}

@Composable
private fun TopField(
    modifier: Modifier = Modifier,
    readOnly: Boolean,
    label: String,
    value: String,
    onChange: (String) -> Unit,
    onNext: () -> Unit,
) {
  OutlinedTextField(
      modifier = modifier,
      readOnly = readOnly,
      value = value,
      onValueChange = onChange,
      keyboardOptions =
          KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next,
          ),
      keyboardActions =
          KeyboardActions(
              onNext = { onNext() },
          ),
      label = {
        Text(
            text = label,
        )
      },
      leadingIcon = {
        Icon(
            modifier = Modifier.padding(end = MaterialTheme.keylines.typography),
            imageVector = Icons.Filled.Tag,
            contentDescription = label,
        )
      },
  )
}

@Composable
private fun BottomField(
    modifier: Modifier = Modifier,
    readOnly: Boolean,
    label: String,
    value: String,
    onChange: (String) -> Unit,
    onNext: () -> Unit,
) {
  OutlinedTextField(
      modifier = modifier,
      readOnly = readOnly,
      value = value,
      onValueChange = onChange,
      keyboardOptions =
          KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next,
          ),
      keyboardActions =
          KeyboardActions(
              onNext = { onNext() },
          ),
      label = {
        Text(
            text = label,
        )
      },
      leadingIcon = {
        Icon(
            modifier = Modifier.padding(end = MaterialTheme.keylines.typography),
            imageVector = Icons.Filled.Paid,
            contentDescription = label,
        )
      },
  )
}

@Composable
private fun PositionToolbar(
    modifier: Modifier = Modifier,
    title: String,
    onClose: () -> Unit,
) {
  Surface(
      modifier = modifier,
      elevation = AppBarDefaults.TopAppBarElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape =
          MaterialTheme.shapes.medium.copy(
              bottomEnd = ZeroCornerSize,
              bottomStart = ZeroCornerSize,
          ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      val contentColor = LocalContentColor.current

      TopAppBar(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.Transparent,
          contentColor = contentColor,
          elevation = ZeroElevation,
          title = {
            Text(
                text = title,
            )
          },
          navigationIcon = {
            IconButton(
                onClick = onClose,
            ) {
              Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Close",
              )
            }
          },
      )
    }
  }
}

@Preview
@Composable
private fun PreviewBasePositionScreen() {
  BasePositionPopup(
      title = "Testing 123",
      isReadOnly = false,
      isSubmitEnabled = true,
      topFieldLabel = "Top",
      topFieldValue = "Top",
      onTopFieldChanged = {},
      bottomFieldLabel = "Bottom",
      bottomFieldValue = "Bottom",
      onBottomFieldChanged = {},
      dateField = null,
      dateLabel = "Date",
      onDateClicked = {},
      onSubmit = {},
      onClose = {},
  )
}
