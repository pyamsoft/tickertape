package com.pyamsoft.tickertape.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberImagePainter

private val MIN_IMAGE_HEIGHT = 120.dp

@Composable
private fun OuchScreen(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    @DrawableRes image: Int,
    illustrationBy: String,
    illustrationLink: String,
    topContent: @Composable ColumnScope.() -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {},
) {
  BlankScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = image,
      illustrationBy = illustrationBy,
      illustrationLink = illustrationLink,
      from = "Ouch!",
      bottomContent = bottomContent,
      topContent = topContent,
  )
}

@Composable
fun KarinaTsoyScreen(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    @DrawableRes image: Int,
    topContent: @Composable ColumnScope.() -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {},
) {
  OuchScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = image,
      illustrationBy = "Karina Tsoy",
      illustrationLink = "https://icons8.com/illustrations/author/602aac63487a405cbf8ba256",
      bottomContent = bottomContent,
      topContent = topContent,
  )
}

@Composable
fun PolinaGolubevaScreen(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    @DrawableRes image: Int,
    topContent: @Composable ColumnScope.() -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {},
) {
  OuchScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = image,
      illustrationBy = "Polina Golubeva",
      illustrationLink = "https://icons8.com/illustrations/author/5f32934501d0360017af905d",
      bottomContent = bottomContent,
      topContent = topContent,
  )
}

@Composable
fun AnnaGoldScreen(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    @DrawableRes image: Int,
    topContent: @Composable ColumnScope.() -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {},
) {
  OuchScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = image,
      illustrationBy = "Anna Golde",
      illustrationLink = "https://icons8.com/illustrations/author/5bf673a26205ee0017636674",
      bottomContent = bottomContent,
      topContent = topContent,
  )
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    topContent: @Composable ColumnScope.() -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {},
) {
  OuchScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = R.drawable.error,
      illustrationBy = "Olha Khomich",
      illustrationLink = "https://icons8.com/illustrations/author/5eb2a7bd01d0360019f124e7",
      bottomContent = bottomContent,
      topContent = topContent,
  )
}

@Composable
fun BlankScreen(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    @DrawableRes image: Int,
    illustrationBy: String,
    illustrationLink: String,
    from: String,
    topContent: @Composable ColumnScope.() -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {},
) {

  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    topContent()

    Image(
        modifier = Modifier.fillMaxWidth().heightIn(min = MIN_IMAGE_HEIGHT),
        painter =
            rememberImagePainter(
                data = image,
                imageLoader = imageLoader,
            ),
        contentScale = ContentScale.FillWidth,
        contentDescription = null,
    )
    Icons8RequiredAttribution(
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
        illustrationBy = illustrationBy,
        illustrationLink = illustrationLink,
        from = from,
        fromLink = "https://icons8.com/illustrations",
    )

    bottomContent()
  }
}
