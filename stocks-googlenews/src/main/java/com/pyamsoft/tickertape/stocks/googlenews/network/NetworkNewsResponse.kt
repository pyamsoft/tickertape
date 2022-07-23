/*
 * Copyright 2021 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.stocks.googlenews.network

import androidx.annotation.CheckResult
import androidx.core.text.HtmlCompat
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Path
import org.simpleframework.xml.Root

/** Can't be a data class because SimpleXML is weird yo */
@Root(name = "rss", strict = false)
internal class NetworkNewsResponse
/** SimpleXML requires an empty constructor */
internal constructor() {

  /** This needs to be a var because SimpleXML is weird yo */
  @get:ElementList(name = "item", inline = true)
  @get:Path("channel")
  @set:ElementList(name = "item", inline = true)
  @set:Path("channel")
  internal var data: List<NewsArticle>? = null

  @get:CheckResult val news = data ?: emptyList()

  /** Can't be a data class because SimpleXML is weird yo */
  @Root(name = "item", strict = false)
  internal class NewsArticle
  /** SimpleXML requires an empty constructor */
  internal constructor() {

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "guid") @set:Element(name = "guid") internal var articleGuid: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "link") @set:Element(name = "link") internal var articleUrl: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "title")
    @set:Element(name = "title")
    internal var articleTitle: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "description")
    @set:Element(name = "description")
    internal var articleDescription: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "pubDate")
    @set:Element(name = "pubDate")
    internal var articlePublishedAt: String? = null

    @get:CheckResult val id = articleGuid.orEmpty()

    @get:CheckResult val publishDate: LocalDateTime?

    @get:CheckResult val title = articleTitle.orEmpty()

    @get:CheckResult val link: String

    @get:CheckResult val newsSource: String

    @get:CheckResult val description: String

    init {
      val p = articlePublishedAt
      val parseFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
      publishDate = if (p == null) null else LocalDateTime.parse(p, parseFormatter)

      val d = articleDescription
      description =
          if (d == null) ""
          else {
            val spanned = HtmlCompat.fromHtml(d, HtmlCompat.FROM_HTML_MODE_LEGACY)
            spanned.toString()
          }

      val u = articleUrl
      val urlData = if (u == null) null else URL(u)
      link = urlData?.toString().orEmpty()
      newsSource = urlData?.host.orEmpty()
    }
  }
}
