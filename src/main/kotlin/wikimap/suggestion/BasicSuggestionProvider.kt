package wikimap.suggestion

import java.io.FileNotFoundException

class BasicSuggestionProvider : SuggestionProvider {
    val seedUrl = "https://en.wikipedia.org"

    override fun getSuggestions(key: String): List<String> {
        val crawler = WebCrawler(seedUrl, 1)
        val link = expandLink(key)

        var suggestedLinks: List<String>

        try {
            suggestedLinks = crawler.crawl(link, 0)
        } catch (ex: FileNotFoundException) {
            suggestedLinks = crawler.getSiteSearchResultLinks(key)
        }

        return suggestedLinks.map { extractArticleName(it) }
    }

    fun expandLink(articleName: String): String =
        seedUrl + "/wiki/" + articleName.replace(" ", "_")

    fun extractArticleName(link: String): String =
        link.substring(link.indexOf("/wiki/") + "/wiki/".length).replace("_", " ")
}