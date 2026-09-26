package com.örnekfilm

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class ÖrnekFilm : MainAPI() {
    override var mainUrl = "https://www.fullhdfilmizlesene.now"
    override var name = "ÖrnekFilm"
    override val hasMainPage = true
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "/filmler" to "Popüler Filmler"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = "https://www.fullhdfilmizlesene.now/filmler?page=$page"
        val document = app.get(url).document
        val items = document.select(".film-item, .movie-item, article").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2, h3, .title")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val poster = this.selectFirst("img")?.attr("src") ?: this.selectFirst("img")?.attr("data-src")
        return newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
            this.posterUrl = poster?.let { fixUrl(it) }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "https://www.fullhdfilmizlesene.now/arama?q={query}".replace("{query}", query)
        val document = app.get(url).document
        return document.select(".film-item, .movie-item, article").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1, .film-title")?.text() ?: "Bilinmeyen"
        val poster = document.selectFirst(".poster img, .film-poster img")?.attr("src")
        val description = document.selectFirst(".description, .film-desc, .ozet")?.text()
        val year = document.selectFirst(".year, .film-year")?.text()?.trim()?.toIntOrNull()

        val episodes = document.select(".player, .video-source, iframe").mapIndexedNotNull { index, el ->
            val src = el.attr("src").ifEmpty { el.attr("data-src") }
            if (src.isBlank()) null
            else newEpisode(fixUrl(src)) { this.name = "Kaynak ${index + 1}" }
        }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster?.let { fixUrl(it) }
            this.plot = description
            this.year = year
            this.episodes = episodes
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        document.select("iframe, video source").forEach { el ->
            val src = el.attr("src").ifEmpty { el.attr("data-src") }
            if (src.startsWith("http")) {
                callback.invoke(newExtractorLink("ÖrnekFilm", "ÖrnekFilm", src) {
                    this.referer = mainUrl
                    this.quality = ExtractorQuality.Unknown
                })
            }
        }
        return true
    }
}
