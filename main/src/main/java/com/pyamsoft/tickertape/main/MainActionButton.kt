package com.pyamsoft.tickertape.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun MainActionButton(
    modifier: Modifier = Modifier,
    show: Boolean,
    page: TopLevelMainPage,
    onActionSelected: (TopLevelMainPage) -> Unit,
) {
  val isFabVisible =
      remember(
          page,
          show,
      ) {
        show && page == TopLevelMainPage.Portfolio
      }

  AnimatedVisibility(
      visible = isFabVisible,
      // Normal FAB animation
      // https://stackoverflow.com/questions/71141501/cant-animate-fab-visible-in-m3-scaffold
      enter = scaleIn(),
      exit = scaleOut(),
  ) {
    FloatingActionButton(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        onClick = { onActionSelected(page) },
    ) {
      Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = "Add",
      )
    }
  }
}
