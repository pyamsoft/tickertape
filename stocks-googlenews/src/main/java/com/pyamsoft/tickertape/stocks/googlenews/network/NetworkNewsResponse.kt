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

/**
 * Can't be a data class because SimpleXML is weird yo
 *
 * Needs to be lazy because SimpleXML will create this class and later update the entries in it.
 */
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

  // Needs to be lazy because SimpleXML will create this class and later update the entries in it.
  @get:CheckResult val news by lazy(LazyThreadSafetyMode.NONE) { data ?: emptyList() }

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

    // Needs to be lazy because SimpleXML will create this class and later update the entries in it.
    @get:CheckResult val id by lazy(LazyThreadSafetyMode.NONE) { articleGuid.orEmpty() }

    // Needs to be lazy because SimpleXML will create this class and later update the entries in it.
    @get:CheckResult
    val publishDate by
        lazy(LazyThreadSafetyMode.NONE) {
          val p = articlePublishedAt
          val parseFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
          return@lazy if (p == null) null else LocalDateTime.parse(p, parseFormatter)
        }

    // Needs to be lazy because SimpleXML will create this class and later update the entries in it.
    @get:CheckResult val title by lazy(LazyThreadSafetyMode.NONE) { articleTitle.orEmpty() }

    // Needs to be lazy because SimpleXML will create this class and later update the entries in it.
    @get:CheckResult
    val description by
        lazy(LazyThreadSafetyMode.NONE) {
          val d = articleDescription
          return@lazy if (d == null) ""
          else {
            val spanned = HtmlCompat.fromHtml(d, HtmlCompat.FROM_HTML_MODE_LEGACY)
            spanned.toString()
          }
        }

    private val articleUrlData by
        lazy(LazyThreadSafetyMode.NONE) {
          val u = articleUrl
          return@lazy if (u == null) null else URL(u)
        }

    // Needs to be lazy because SimpleXML will create this class and later update the entries in it.
    @get:CheckResult
    val link by lazy(LazyThreadSafetyMode.NONE) { articleUrlData?.toString().orEmpty() }

    // Needs to be lazy because SimpleXML will create this class and later update the entries in it.
    @get:CheckResult
    val newsSource by lazy(LazyThreadSafetyMode.NONE) { articleUrlData?.host.orEmpty() }
  }
}
