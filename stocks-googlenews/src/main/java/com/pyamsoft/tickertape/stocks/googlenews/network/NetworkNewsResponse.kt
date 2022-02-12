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

  private val articles by lazy(LazyThreadSafetyMode.NONE) { data ?: emptyList() }

  @CheckResult
  fun news(): List<NewsArticle> {
    return articles
  }

  /** Can't be a data class because SimpleXML is weird yo */
  @Root(name = "item", strict = false)
  internal class NewsArticle
  /** SimpleXML requires an empty constructor */
  internal constructor() {

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "guid") @set:Element(name = "guid") internal var guid: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "link") @set:Element(name = "link") internal var url: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "title") @set:Element(name = "title") internal var title: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "description")
    @set:Element(name = "description")
    internal var description: String? = null

    /** This needs to be a var because SimpleXML is weird yo */
    @get:Element(name = "pubDate")
    @set:Element(name = "pubDate")
    internal var publishedAt: String? = null

    private val id by
        lazy(LazyThreadSafetyMode.NONE) {
          return@lazy guid.orEmpty()
        }

    private val publishDate by
        lazy(LazyThreadSafetyMode.NONE) {
          val p = publishedAt
          val parseFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
          return@lazy if (p == null) null else LocalDateTime.parse(p, parseFormatter)
        }

    private val sanitizedTitle by lazy(LazyThreadSafetyMode.NONE) { title.orEmpty() }

    private val urlData by
        lazy(LazyThreadSafetyMode.NONE) {
          val u = url
          return@lazy if (u == null) null else URL(u)
        }

    private val link by lazy(LazyThreadSafetyMode.NONE) { urlData?.toString().orEmpty() }

    private val source by lazy(LazyThreadSafetyMode.NONE) { urlData?.host.orEmpty() }

    private val sanitizedDescription by
        lazy(LazyThreadSafetyMode.NONE) {
          val d = description
          return@lazy if (d == null) ""
          else {
            val spanned = HtmlCompat.fromHtml(d, HtmlCompat.FROM_HTML_MODE_LEGACY)
            spanned.toString()
          }
        }

    @CheckResult
    fun id(): String {
      return id
    }

    @CheckResult
    fun publishDate(): LocalDateTime? {
      return publishDate
    }

    @CheckResult
    fun title(): String {
      return sanitizedTitle
    }

    @CheckResult
    fun description(): String {
      return sanitizedDescription
    }

    @CheckResult
    fun newsSource(): String {
      return source
    }

    @CheckResult
    fun link(): String {
      return link
    }
  }
}
