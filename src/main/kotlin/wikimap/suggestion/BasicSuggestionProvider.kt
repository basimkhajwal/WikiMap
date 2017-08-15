package wikimap.suggestion

import java.io.FileNotFoundException

class BasicSuggestionProvider : SuggestionProvider {
    val seedUrl = "https://en.wikipedia.org"

    override fun getSuggestions(key: String): List<String> {
        val crawler = WebCrawler(seedUrl, 1)
        val link = expandLink(key)

        var suggestedLinks:List<String>

        try {
            suggestedLinks = crawler.crawl(link, 0)
        }catch (ex:FileNotFoundException){
            val searchTerm = extractArticleNames(listOf(link)).first()
            suggestedLinks = crawler.getSiteSearchResultLinks(searchTerm)
        }

        return extractArticleNames(suggestedLinks)
    }

    fun expandLink(articleName: String): String = seedUrl + "/wiki/" + articleName.replace(" ", "_")

    fun extractArticleNames(links: List<String>): List<String> =
        links.map {
            it.substring(it.indexOf("/wiki/") + "/wiki/".length)
              .replace("_", " ")
        }
}