package wikimap.suggestion

import java.io.FileNotFoundException

class BasicSuggestionProvider : SuggestionProvider {
    private val seedUrl = "https://en.wikipedia.org"

    override fun getSuggestions(key: String): List<String> {
        val crawler = WebCrawler(seedUrl)
        val link = expandLink(key)

        var suggestedLinks: List<String>

        try {
            suggestedLinks = crawler.crawl(link, 1).filterNot { it == link }
        } catch (ex: FileNotFoundException) {
            suggestedLinks = crawler.getSiteSearchResultLinks(key)
        }

        return suggestedLinks.map { extractArticleName(it) }
    }

    private fun expandLink(articleName: String): String =
        seedUrl + "/wiki/" + articleName.replace(" ", "_")

    private fun extractArticleName(link: String): String =
        link.substring(link.indexOf("/wiki/") + "/wiki/".length).replace("_", " ")
}
